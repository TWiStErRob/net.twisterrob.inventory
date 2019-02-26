package net.twisterrob.test.frameworks;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;
import org.junit.rules.TestName;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.view.View;

import net.twisterrob.test.frameworks.classes.*;

/**
 * Base class for {@link PowerMockito PowerMock} tests. Simply to prevent duplication.
 * It can be used by multiple setup approaches and tests basic features of PowerMock.
 */
//@PowerMockIgnore({
//		"org.mockito.*", "org.powermock.*", "org.powermock.api.mockito.repackaged.*", "com.thoughtworks.xstream.*",
//		"java.*", "javax.*", "org.junit.*", "org.gradle.*",
//		"org.fusesource.jansi.*", "org.w3c.*",
//		"org.robolectric.*", "android.*", "org.apache.*", "org.json.*"
//})
public abstract class PowerMockTests {
	/**
	 * Used to generate something unique to be used in stubbing for each test.
	 */
	@Rule public final TestName test = new TestName();

	@PrepareOnlyThisForTest(Final.class)
	public static abstract class Inner {
		@Rule public final TestName test = new TestName();

		@Mock Final mockFinal;

		@Test public void testAnnotations() {
			assertNotNull(mockFinal);
			assertTrue(Mockito.mockingDetails(mockFinal).isMock());
		}

		/** Check that final classes/methods can be mocked. */
		@Test public void testFinal_doReturnWhen_onAnnotated() {
			//noinspection ResultOfMethodCallIgnored it's stubbing
			PowerMockito.doReturn(test.getMethodName()).when(mockFinal).finalMethod();

			String result = mockFinal.finalMethod();

			assertEquals(test.getMethodName(), result);
		}

		/** Check that final classes/methods can be mocked. */
		@Test public void testFinal_whenThenReturn_onAnnotated() {
			PowerMockito.when(mockFinal.finalMethod()).thenReturn(test.getMethodName());

			String result = mockFinal.finalMethod();

			assertEquals(test.getMethodName(), result);
		}
	}

	/** Check that final classes/methods can be mocked. */
	@PrepareOnlyThisForTest(Final.class)
	@Test public void testFinal_whenThenReturn() {
		Final mock = PowerMockito.mock(Final.class);
		PowerMockito.when(mock.finalMethod()).thenReturn(test.getMethodName());

		String result = mock.finalMethod();

		assertEquals(test.getMethodName(), result);
		//noinspection ResultOfMethodCallIgnored it's a verify()
		Mockito.verify(mock).finalMethod();
	}

	/** Check that final classes/methods can be mocked. */
	@PrepareOnlyThisForTest(Final.class)
	@Test public void testFinal_doReturnWhen() {
		Final mock = PowerMockito.mock(Final.class);
		//noinspection ResultOfMethodCallIgnored it's stubbing
		PowerMockito.doReturn(test.getMethodName()).when(mock).finalMethod();

		String result = mock.finalMethod();

		assertEquals(test.getMethodName(), result);
	}

	/** Check that the new operator can be mocked. */
	@PrepareOnlyThisForTest(New.class)
	@Test public void testNew() throws Exception {
		New mock = PowerMockito.mock(New.class);
		PowerMockito.when(mock.getter()).thenReturn(test.getMethodName());
		PowerMockito.whenNew(New.class).withNoArguments().thenReturn(mock);

		String result = new New().getter();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyNew(New.class).withNoArguments();
	}

	/** Check that static methods can be mocked. */
	@PrepareOnlyThisForTest(Static.class)
	@Test public void testStatic() {
		PowerMockito.mockStatic(Static.class);
		PowerMockito.when(Static.staticMethod()).thenReturn(test.getMethodName());

		String result = Static.staticMethod();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyStatic(Static.class);
		//noinspection ResultOfMethodCallIgnored it's a verify()
		Static.staticMethod();
	}

	/** Check that static methods can be mocked. */
	@PrepareOnlyThisForTest(Static.class)
	@Test public void testStatic_reflective() throws Exception {
		PowerMockito.mockStatic(Static.class);
		PowerMockito.when(Static.class, "staticMethod").thenReturn(test.getMethodName());

		String result = Static.staticMethod();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyStatic(Static.class);
		//noinspection ResultOfMethodCallIgnored it's a verify()
		Static.staticMethod();
	}

	/** Check that private methods can be mocked. */
	@PrepareOnlyThisForTest(Private.class)
	@Test public void testPrivate() throws Exception {
		Private mock = PowerMockito.spy(new Private());
		PowerMockito.doReturn(test.getMethodName()).when(mock, "privateMethod");

		String result = mock.publicMethod();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyPrivate(mock).invoke("privateMethod");
	}

	/** Check that private static methods can be mocked. */
	@PrepareOnlyThisForTest(StaticPrivate.class)
	@Test public void testStaticPrivate_whenThenReturn() throws Exception {
		PowerMockito.spy(StaticPrivate.class);
		PowerMockito.when(StaticPrivate.class, "privateStaticMethod").thenReturn(test.getMethodName());

		String result = StaticPrivate.publicStaticMethod();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyPrivate(StaticPrivate.class).invoke("privateStaticMethod");
	}

	/** Check that private static methods can be mocked. */
	@PrepareOnlyThisForTest(StaticPrivate.class)
	@Test public void testStaticPrivate_doReturnWhen() throws Exception {
		PowerMockito.spy(StaticPrivate.class);
		PowerMockito.doReturn(test.getMethodName()).when(StaticPrivate.class, "privateStaticMethod");

		String result = StaticPrivate.publicStaticMethod();

		assertEquals(test.getMethodName(), result);
		PowerMockito.verifyPrivate(StaticPrivate.class).invoke("privateStaticMethod");
	}

	@PrepareOnlyThisForTest(AtomicBoolean.class)
	@Test public void testMockNewJavaClass() throws Exception {
		AtomicBoolean mock = Mockito.mock(AtomicBoolean.class);
		assertNotNull("Mockito should create mocks as usual", mock);
		PowerMockito.whenNew(AtomicBoolean.class).withArguments(false).thenReturn(mock);

		AtomicBoolean powerNew = new AtomicBoolean(false);

		assertThat(powerNew, sameInstance(mock));
		assertThat(new AtomicBoolean(true), not(sameInstance(powerNew)));
	}

	@PrepareOnlyThisForTest(Handler.class)
	@Test public void testMockNewAndroidClass() throws Exception {
		Looper looper = Mockito.mock(Looper.class);
		Handler handler = Mockito.mock(Handler.class);
		assertNotNull("Mockito should create mocks as usual", handler);
		PowerMockito.whenNew(Handler.class).withArguments(looper).thenReturn(handler);

		Handler powerNew = new Handler(looper);

		assertThat(powerNew, sameInstance(handler));
		assertThat(new Handler(), not(sameInstance(powerNew)));
	}

	@Test public void testMockNormalAndroidClass() {
		View.OnClickListener androidMock = PowerMockito.mock(View.OnClickListener.class);
		RuntimeException ex = new RuntimeException("test");
		PowerMockito.doThrow(ex).when(androidMock).onClick(Mockito.<View>any()); // void method

		try {
			androidMock.onClick(null);
			fail("Exception expected, doThrow-when should've stubbed this call");
		} catch (RuntimeException e) {
			assertSame("doThrow-when should've stubbed this call", ex, e);
		}
	}

	@Test public void testMockFinalAndroidClass() {
		SQLiteDatabase androidFinalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPageSize()).thenReturn(12345L);

		long result = androidFinalMock.getPageSize();

		assertEquals("when-thenReturn should've stubbed this call", 12345L, result);
	}

	@Test public void testPowerMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPath()).thenReturn(test.getMethodName());

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", test.getMethodName(), result);
	}

	@Test public void testMockitoMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = Mockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPath()).thenReturn(test.getMethodName());

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", test.getMethodName(), result);
	}
}
