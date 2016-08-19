package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.test.frameworks.classes.*;

// TOFIX PowerMock doesn't work with Robolectric 3.1-3.1.2: https://github.com/robolectric/robolectric/issues/2429
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore(
		{
				"java.*", "javax.*", "org.w3c.*",
				"org.junit.*", "org.mockito.*", "org.powermock.*", "org.gradle.*",
				"com.thoughtworks.xstream.*", "org.fusesource.jansi.*",
//				"org.robolectric.*", "android.*", "org.apache.*"
		}
)
@PrepareForTest({Final.class, SQLiteDatabase.class}) // makes no difference?
@Ignore // Didn't manage to make it work yet
public class RobolectricTest_PowerMock {
	// Even with 3.0 there are issues
	// @Rule public PowerMockRule rule = new PowerMockRule();
	@Rule public ExpectedException thrown = ExpectedException.none();
	@Mock private OnClickListener mock;
	@InjectMocks private AndroidRecipient target;

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
	@Test public void testAnnotations() {
		assertNotNull(mock);
		assertNotNull(target);
		assertSame(mock, target.getMockable());
	}

	/** Check that special class can be mocked. */
	@Test public void testPowermock() {
		Final mock = PowerMockito.mock(Final.class);
		PowerMockito.when(mock.finalMethod()).thenReturn("mocked");

		String result = mock.finalMethod();

		assertEquals("mocked", result);
	}

	/** Call an Android class without mocking. */
	@Test public void testAndroid() {
		Bundle bundle = new Bundle();
		bundle.putString("test", "value");

		assertEquals("value", bundle.getString("test"));
	}

	/** Call an Android class without mocking. */
	@Test public void testMockAndroid() {
		OnClickListener mock = Mockito.mock(OnClickListener.class);
		RuntimeException ex = new RuntimeException("test");
		Mockito.doThrow(ex).when(mock).onClick(any(View.class));

		try {
			mock.onClick(null);
			fail("Exception expected");
		} catch (RuntimeException e) {
			assertSame(e, ex);
		}
	}

	/** Call an Android class without mocking. */
	@Test public void testPowermockAndroid() {
		SQLiteDatabase finalMock = PowerMockito.mock(SQLiteDatabase.class);
		PowerMockito.when(finalMock.getPath()).thenReturn("mocked");

		String result = finalMock.getPath();

		assertEquals("mocked", result);
	}
}
