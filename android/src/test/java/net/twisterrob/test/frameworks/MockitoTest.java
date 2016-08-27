package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.mockito.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.twisterrob.test.frameworks.classes.*;

public class MockitoTest {
	@Rule public MockitoRule mockito = MockitoJUnit.rule();
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Mockable mock;
	@InjectMocks
	private Recipient target;

	/** Check if @Test methods are executed. */
	@Test public void testRunner() {
		// all ok, it just needs to be called
		assertTrue(true);
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
		Mockable mock = Mockito.mock(Mockable.class);
		doThrow(new RuntimeException("test")).when(mock).method();

		thrown.expect(RuntimeException.class);
		thrown.expectMessage("test");
		mock.method();
	}

	/** Check if MockitoAnnotations.injectMocks(this) is called. */
	@Test public void testAutoMocks() {
		assertNotNull(mock);
		assertNotNull(target);
		assertSame(mock, target.getMockable());
	}
}
