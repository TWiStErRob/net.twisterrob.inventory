package net.twisterrob.test.frameworks;

import org.junit.Test;
import org.mockito.*;

import static org.junit.Assert.*;

import android.os.Bundle;
import android.view.View.OnClickListener;

import net.twisterrob.test.frameworks.classes.AndroidRecipient;

public class RobolectricTest extends RobolectricTestBase {
	@Mock
	private OnClickListener mock;
	@InjectMocks
	private AndroidRecipient target;

	/** Check if @Test methods are executed. */
	@Test public void testRunner() {
		assertTrue(true);
	}

	/** Check if JUnit rules work. */
	@Test public void testJUnit() {
		assertNotNull(thrown);

		thrown.expect(NullPointerException.class);
		thrown.expectMessage("test");
		throw new NullPointerException("test");
	}

	/** Check if auto-mocking works. */
	@Test public void testMockito() {
		assertNotNull(mock);
		assertNotNull(target);
		assertSame(mock, target.getMockable());
	}

	/** Call an Android class without mocking. */
	@Test public void testAndroid() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
	}
}
