package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.test.frameworks.classes.*;

// TOCHECK https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/test/java/com/facebook/FacebookPowerMockTestCase.java
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
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
@PrepareForTest({Final.class, SQLiteDatabase.class}) // makes no difference?
public class RobolectricTest_PowerMock {
	private final PowerMockRule power = new PowerMockRule();
	private final ExpectedException thrown = ExpectedException.none();
	@Rule public RuleChain rules = RuleChain
			.emptyRuleChain()
			.around(new MethodRuleAdapter(power))
			.around(thrown);
	@Mock OnClickListener mockAndroidClass;
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

	@Ignore("PowerMockRule does not seem to inject fields in 1.6.6")
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

	@Ignore("Why does not work?") // FIXME figure out why it breaks
	@Test public void testMockNormalAndroidClass() {
		OnClickListener androidMock = PowerMockito.mock(OnClickListener.class);
		RuntimeException ex = new RuntimeException("test");
		PowerMockito.doThrow(ex).when(androidMock).onClick(any(View.class)); // void method

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

	@Test public void testMockFinalAndroidClassAndMethod() {
		SQLiteDatabase androidFinalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(androidFinalMock.getPath()).thenReturn("mocked");

		String result = androidFinalMock.getPath();

		assertEquals("when-thenReturn should've stubbed this call", "mocked", result);
	}
}
