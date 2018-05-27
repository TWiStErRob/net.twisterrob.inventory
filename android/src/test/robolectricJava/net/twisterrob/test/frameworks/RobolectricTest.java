package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.TestName;
import org.mockito.*;

import static org.junit.Assert.*;

import android.os.Bundle;
import android.view.View.OnClickListener;

import net.twisterrob.test.frameworks.classes.AndroidRecipient;

public class RobolectricTest extends RobolectricTestBase {
	@Mock OnClickListener mockAndroidClass;
	@InjectMocks AndroidRecipient sut;
	@Rule public TestName testName = new TestName();

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	@Test public void testJUnitRule() {
		assertNotNull(testName);
		assertEquals("testJUnitRule", testName.getMethodName());
	}

	@Test public void testMockito() {
		assertNotNull("@Mock field should be initialized", mockAndroidClass);
		assertNotNull("@InjectMocks field should be initialized", sut);
		assertSame("@Mock should be injected into @InjectMocks object", mockAndroidClass, sut.getMockable());
	}

	@Test public void testAndroidClassViaRobolectric() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
	}
}
