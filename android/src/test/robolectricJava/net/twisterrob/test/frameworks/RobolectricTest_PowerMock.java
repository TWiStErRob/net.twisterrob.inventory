package net.twisterrob.test.frameworks;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.Mock;
import org.mockito.junit.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.view.View;

import net.twisterrob.test.frameworks.classes.*;

// TOCHECK https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/test/java/com/facebook/FacebookPowerMockTestCase.java
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore(
		{
				"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"
//				"java.*", "javax.*", "org.w3c.*",
//				"org.junit.*", "org.mockito.*", "org.powermock.*", "org.gradle.*",
//				"com.thoughtworks.xstream.*", "org.fusesource.jansi.*",
//				"org.robolectric.*", /*"android.*",*/ "org.apache.*", "org.json.*"
		}
)
@PrepareForTest({Final.class, Handler.class, AtomicBoolean.class})
public class RobolectricTest_PowerMock {
	private final PowerMockRule power = new PowerMockRule();
	private final MockitoRule mockito = MockitoJUnit.rule();
	private final ExpectedException thrown = ExpectedException.none();
	@Rule public MethodRuleChain rules = MethodRuleChain
			.emptyRuleChain()
			.around(mockito)
			.around(power)
			.around(new TestRuleAdapter(thrown));
	@Mock View.OnClickListener mockAndroidClass;
	@InjectMocks AndroidRecipient sut;

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	@Test public void testJUnitThrowRule() {
		assertNotNull("ExpectedException rule should be initialized", thrown);

		thrown.expect(NullPointerException.class);
		thrown.expectMessage("test");
		throw new NullPointerException("test");
	}

	@Test public void testMockitoAnnotations() {
		assertNotNull("@Mock field should be initialized", mockAndroidClass);
		assertNotNull("@InjectMocks field should be initialized", sut);
		assertSame("@Mock should be injected into @InjectMocks object", mockAndroidClass, sut.getMockable());
	}

	@Test public void testMockFinalAppClass() {
		Final finalMock = PowerMockito.mock(Final.class);
		PowerMockito.when(finalMock.finalMethod()).thenReturn("mocked");

		String result = finalMock.finalMethod();

		assertEquals("mocked", result);
	}

	@Test public void testRobolectricAndroid_NoMocking() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
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

	@Test public void testMockNewAndroidClass() throws Exception {
		PowerMockito.mockStatic(Handler.class);
		Looper looper = Mockito.mock(Looper.class);
		Handler handler = Mockito.mock(Handler.class);
		assertNotNull("Mockito should create mocks as usual", handler);
		PowerMockito.whenNew(Handler.class).withArguments(looper).thenReturn(handler);

		Handler powerNew = new Handler(looper);

		assertThat(powerNew, sameInstance(handler));
		assertThat(new Handler(), not(sameInstance(powerNew)));
	}

	@Test public void testMockNewJavaClass() throws Exception {
		PowerMockito.mockStatic(AtomicBoolean.class);
		AtomicBoolean mock = Mockito.mock(AtomicBoolean.class);
		assertNotNull("Mockito should create mocks as usual", mock);
		PowerMockito.whenNew(AtomicBoolean.class).withArguments(false).thenReturn(mock);

		AtomicBoolean powerNew = new AtomicBoolean(false);

		assertThat(powerNew, sameInstance(mock));
		assertThat(new AtomicBoolean(true), not(sameInstance(powerNew)));
	}

	@Test public void testMockFinalAndroidClass() {
		SQLiteDatabase androidFinalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPageSize()).thenReturn(12345L);

		long result = androidFinalMock.getPageSize();

		assertEquals("when-thenReturn should've stubbed this call", 12345L, result);
	}

	@Test public void testPowerMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPath()).thenReturn("mocked");

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", "mocked", result);
	}

	@Test public void testMockitoMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = Mockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPath()).thenReturn("mocked");

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", "mocked", result);
	}
}
