package net.twisterrob.test.frameworks;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import net.twisterrob.inventory.android.TestIgnoreApp;
import net.twisterrob.test.frameworks.classes.AndroidRecipient;

/**
 * Robolectric setup copied from {@link RobolectricTestBase} to be able to extend {@link PowerMockTests}.
 */
// TOCHECK https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/test/java/com/facebook/FacebookPowerMockTestCase.java
@RunWith(RobolectricTestRunner.class)
@Config(application = TestIgnoreApp.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class RobolectricTest_PowerMock extends PowerMockTests {

	@Rule public final MockitoRule mockito = MockitoJUnit.rule();

	@Mock View.OnClickListener mockAndroidClass;
	@InjectMocks AndroidRecipient sut;

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	@Test public void testMockitoAnnotations() {
		assertNotNull("@Mock field should be initialized", mockAndroidClass);
		assertThat(mockAndroidClass, isMock());
		assertNotNull("@InjectMocks field should be initialized", sut);
		assertThat(sut, not(isMock()));
		assertSame("@Mock should be injected into @InjectMocks object", mockAndroidClass, sut.getMockable());
	}

	@Test public void testRobolectricAndroid_NoMocking() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
	}
}
