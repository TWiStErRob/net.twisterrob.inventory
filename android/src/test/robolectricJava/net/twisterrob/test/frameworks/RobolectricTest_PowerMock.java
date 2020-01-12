package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import android.os.Bundle;
import android.view.View;

import net.twisterrob.test.frameworks.classes.AndroidRecipient;

/**
 * Robolectric setup copied from {@link RobolectricTestBase} to be able to extend {@link PowerMockTests}.
 */
// TOCHECK https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/test/java/com/facebook/FacebookPowerMockTestCase.java
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore({
		"org.mockito.*", "org.powermock.*", "org.robolectric.*", "android.*", "org.json.*",
//		"org.mockito.*", "org.powermock.*", "org.powermock.api.mockito.repackaged.*", "com.thoughtworks.xstream.*",
//		"java.*", "javax.*", "jdk.*", "com.sun.*", "org.w3c.*",
//		"org.junit.*", "org.gradle.*", "org.fusesource.jansi.*", 
//		"org.robolectric.*", "android.*", "org.apache.*", "org.json.*",
//		// https://github.com/powermock/powermock/issues/864
//		"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"
})
public class RobolectricTest_PowerMock extends PowerMockTests {

	private final PowerMockRule power = new PowerMockRule();
	private final MockitoRule mockito = MockitoJUnit.rule();

	@Rule public MethodRuleChain rules = MethodRuleChain
			.emptyRuleChain()
			.around(mockito)
			.around(power);

	@Mock View.OnClickListener mockAndroidClass;
	@InjectMocks AndroidRecipient sut;

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	@Test public void testMockitoAnnotations() {
		assertNotNull("@Mock field should be initialized", mockAndroidClass);
		assertTrue(Mockito.mockingDetails(mockAndroidClass).isMock());
		assertNotNull("@InjectMocks field should be initialized", sut);
		assertFalse(Mockito.mockingDetails(sut).isMock());
		assertSame("@Mock should be injected into @InjectMocks object", mockAndroidClass, sut.getMockable());
	}

	@Test public void testRobolectricAndroid_NoMocking() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
	}

	@Ignore("Fails on latest test frameworks combination")
	@Override @Test public void testNew() throws Exception {
		super.testNew();
	}

	@Ignore("Fails on latest test frameworks combination")
	@Override @Test public void testMockNewJavaClass() throws Exception {
		super.testMockNewJavaClass();
	}

	@Ignore("Fails on latest test frameworks combination")
	@Override @Test public void testMockNewAndroidClass() throws Exception {
		super.testMockNewAndroidClass();
	}
}
