package net.twisterrob.test.frameworks;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockingDetails;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import net.twisterrob.test.frameworks.classes.Final;
import net.twisterrob.test.frameworks.classes.New;
import net.twisterrob.test.frameworks.classes.Private;
import net.twisterrob.test.frameworks.classes.Static;
import net.twisterrob.test.frameworks.classes.StaticPrivate;

/**
 * Base class for advanced {@link org.mockito.Mockito} tests. Simply to prevent duplication.
 * It can be used by multiple setup approaches and tests features of {@link org.mockito.Mockito}.
 * Originally this was backed by PowerMock, which is unmaintained.
 * Luckily Mockito inline mock-maker already supports most features.
 */
public abstract class PowerMockTests {
	/**
	 * Used to generate something unique to be used in stubbing for each test.
	 */
	@Rule public final TestName test = new TestName();

	public static abstract class Inner {
		@Rule public final TestName test = new TestName();

		@Mock Final mockFinal;

		@Test public void testAnnotations() {
			assertNotNull(mockFinal);
			assertTrue(mockingDetails(mockFinal).isMock());
		}

		/** Check that final classes/methods can be mocked. */
		@Test public void testFinal_doReturnWhen_onAnnotated() {
			//noinspection ResultOfMethodCallIgnored it's stubbing
			doReturn(test.getMethodName()).when(mockFinal).finalMethod();

			String result = mockFinal.finalMethod();

			assertEquals(test.getMethodName(), result);
		}

		/** Check that final classes/methods can be mocked. */
		@Test public void testFinal_whenThenReturn_onAnnotated() {
			when(mockFinal.finalMethod()).thenReturn(test.getMethodName());

			String result = mockFinal.finalMethod();

			assertEquals(test.getMethodName(), result);
		}
	}

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinal_whenThenReturn() {
		Final mock = mock(Final.class);
		when(mock.finalMethod()).thenReturn(test.getMethodName());

		String result = mock.finalMethod();

		assertEquals(test.getMethodName(), result);
		//noinspection ResultOfMethodCallIgnored it's a verify()
		verify(mock).finalMethod();
	}

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinal_doReturnWhen() {
		Final mock = mock(Final.class);
		//noinspection ResultOfMethodCallIgnored it's stubbing
		doReturn(test.getMethodName()).when(mock).finalMethod();

		String result = mock.finalMethod();

		assertEquals(test.getMethodName(), result);
	}

	/** Check that the new operator can be mocked. */
	@Test public void testNew() {
		MockedConstruction.MockInitializer<New> stubs = (mock, context) ->
				when(mock.getter()).thenReturn(test.getMethodName());
		try (MockedConstruction<New> mockedNew = mockConstruction(New.class, stubs)) {
			String result = new New().getter();

			assertEquals(test.getMethodName(), result);
			assertThat(mockedNew.constructed(), hasSize(1));
		}
	}

	/** Check that static methods can be mocked. */
	@Test public void testStatic() {
		try (MockedStatic<Static> mockedStatic = mockStatic(Static.class)) {
			mockedStatic.when(Static::staticMethod).thenReturn(test.getMethodName());

			String result = Static.staticMethod();

			assertEquals(test.getMethodName(), result);
			mockedStatic.verify(Static::staticMethod);
		}
	}

	/** Check that static methods can be mocked. */
	@Test public void testStatic_reflective() {
		MockedStatic.Verification callStaticMethod = () ->
				Static.class.getDeclaredMethod("staticMethod").invoke(null);
		try (MockedStatic<Static> mockedStatic = mockStatic(Static.class)) {
			mockedStatic.when(callStaticMethod).thenReturn(test.getMethodName());

			String result = Static.staticMethod();

			assertEquals(test.getMethodName(), result);
			mockedStatic.verify(Static::staticMethod);
		}
	}

	/** Check that private methods can be mocked. */
	@Ignore("Mockito doesn't support mocking private methods, PowerMock did.")
	@Test public void testPrivate() throws Exception {
		Private mock = spy(new Private());
		Method privateMethod = Private.class.getDeclaredMethod("privateMethod");
		privateMethod.setAccessible(true);
		privateMethod.invoke(doReturn(test.getMethodName()).when(mock));

		String result = mock.publicMethod();

		assertEquals(test.getMethodName(), result);
		privateMethod.invoke(verify(mock));
	}

	/** Check that private static methods can be mocked. */
	@SuppressWarnings({"try", "unused"})
	@Ignore("Mockito doesn't support mocking private methods, PowerMock did.")
	@Test public void testStaticPrivate_whenThenReturn() {
		Answer<Object> spy = CALLS_REAL_METHODS;
		try (MockedStatic<StaticPrivate> mockedStatic = mockStatic(StaticPrivate.class, spy)) {
			//PowerMockito.when(StaticPrivate.class, "privateStaticMethod").thenReturn(test.getMethodName());

			String result = StaticPrivate.publicStaticMethod();

			assertEquals(test.getMethodName(), result);
			//PowerMockito.verifyPrivate(StaticPrivate.class).invoke("privateStaticMethod");
		}
	}

	/** Check that private static methods can be mocked. */
	@SuppressWarnings({"try", "unused"})
	@Ignore("Mockito doesn't support mocking private methods, PowerMock did.")
	@Test public void testStaticPrivate_doReturnWhen() {
		Answer<Object> spy = CALLS_REAL_METHODS;
		try (MockedStatic<StaticPrivate> mockedStatic = mockStatic(StaticPrivate.class, spy)) {
			//PowerMockito.doReturn(test.getMethodName()).when(StaticPrivate.class, "privateStaticMethod");

			String result = StaticPrivate.publicStaticMethod();

			assertEquals(test.getMethodName(), result);
			//PowerMockito.verifyPrivate(StaticPrivate.class).invoke("privateStaticMethod");
		}
	}

	@Test public void testMockNewJavaClass() {
		try (MockedConstruction<AtomicBoolean> mockedNew = mockConstruction(AtomicBoolean.class)) {
			AtomicBoolean powerNew = new AtomicBoolean(false);

			assertThat(powerNew, isMock());
			assertEquals(mockedNew.constructed(), Collections.singletonList(powerNew));
			assertThat(new AtomicBoolean(true), not(sameInstance(powerNew)));
		}
	}

	@Test public void testMockNewAndroidClass() {
		Looper looper = mock(Looper.class);
		MockedConstruction.MockInitializer<Handler> init = (mock, context) ->
				when(mock.getLooper()).thenReturn(looper);
		try (MockedConstruction<Handler> mockedNew = mockConstruction(Handler.class, init)) {

			Handler powerNew = new Handler(looper);

			assertThat(powerNew, isMock());
			assertEquals(mockedNew.constructed(), Collections.singletonList(powerNew));
			assertThat(powerNew.getLooper(), sameInstance(looper));
			assertThat(new Handler(looper), not(sameInstance(powerNew)));
		}
	}

	@Test public void testMockNormalAndroidClass() {
		View.OnClickListener androidMock = mock(View.OnClickListener.class);
		RuntimeException ex = new RuntimeException("test");
		doThrow(ex).when(androidMock).onClick(any()); // void method

		try {
			androidMock.onClick(null);
			fail("Exception expected, doThrow-when should've stubbed this call");
		} catch (RuntimeException e) {
			assertSame("doThrow-when should've stubbed this call", ex, e);
		}
	}

	@Test public void testMockFinalAndroidClass() {
		SQLiteDatabase androidFinalMock = mock(SQLiteDatabase.class);
		when(androidFinalMock.getPageSize()).thenReturn(12345L);

		long result = androidFinalMock.getPageSize();

		assertEquals("when-thenReturn should've stubbed this call", 12345L, result);
	}

	@Test public void testPowerMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = mock(SQLiteDatabase.class);
		when(androidFinalMock.getPath()).thenReturn(test.getMethodName());

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", test.getMethodName(), result);
	}

	@Test public void testMockitoMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = mock(SQLiteDatabase.class);
		when(androidFinalMock.getPath()).thenReturn(test.getMethodName());

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", test.getMethodName(), result);
	}
	
	protected static @NonNull Matcher<Object> isMock() {
		return new DiagnosingMatcher<Object>() {
			@Override public void describeTo(Description description) {
				description.appendText("is mock");
			}
			@Override protected boolean matches(Object item, Description mismatchDescription) {
				MockingDetails details = mockingDetails(item);
				if (!details.isMock()) {
					mismatchDescription.appendText("Not a mock: ").appendValue(item);
					return false;
				}
				return true;
			}
		};
	}
}
