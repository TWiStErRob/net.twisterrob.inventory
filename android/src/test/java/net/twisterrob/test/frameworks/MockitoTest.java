package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.mockito.*;
import org.mockito.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.twisterrob.test.frameworks.classes.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class MockitoTest {
	@Rule public MockitoRule mockito = MockitoJUnit.rule();

	@Mock Mockable mock;

	@InjectMocks Recipient target;

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	/** Check if explicitly created mocks work. */
	@Test public void testExplicitMock() {
		assertNotNull(Mockito.mock(Mockable.class));
	}

	/** Check if method call can be verified. */
	@Test public void testVerify() {
		Mockable mock = Mockito.mock(Mockable.class);

		mock.method();

		verify(mock).method();
	}

	/** Check if method call can be mocked. */
	@Test public void testWhen() {
		final Mockable mock = Mockito.mock(Mockable.class);
		doThrow(new RuntimeException("test")).when(mock).method();

		RuntimeException expectedFailure = assertThrows(RuntimeException.class, new ThrowingRunnable() {
			@Override public void run() {
				mock.method();
			}
		});

		assertThat(expectedFailure, hasMessage("test"));
	}

	/** Check if MockitoAnnotations.injectMocks(this) is called. */
	@Test public void testAutoMocks() {
		assertNotNull(mock);
		assertNotNull(target);
		assertSame(mock, target.getMockable());
	}
}
