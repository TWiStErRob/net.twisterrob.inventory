package net.twisterrob.test.frameworks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.twisterrob.test.frameworks.classes.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Final.class, Static.class, Private.class, New.class})
public class PowerMockTest_Runner extends MockitoTest {
	@Mock Final finalMock;

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinal() {
		Final mock = PowerMockito.mock(Final.class);
		PowerMockito.when(mock.finalMethod()).thenReturn("mocked");

		String result = mock.finalMethod();

		assertEquals("mocked", result);
	}

	/** Check that static methods can be mocked. */
	@Test public void testStatic() throws Exception {
		PowerMockito.mockStatic(Static.class);
		PowerMockito.when(Static.class, "staticMethod").thenReturn("mocked");

		String result = Static.staticMethod();

		assertEquals("mocked", result);
		//PowerMockito.verifyStatic(); // cannot mock and verify at the same time
	}

	/** Check that private methods can be mocked. */
	@Test public void testPrivate() throws Exception {
		Private mock = PowerMockito.spy(new Private());
		PowerMockito.doReturn("mocked").when(mock, "privateMethod");

		String result = mock.publicMethod();

		assertEquals("mocked", result);
		PowerMockito.verifyPrivate(mock).invoke("privateMethod");
	}

	/** Check that the new operator can be mocked. */
	@Test public void testNew() throws Exception {
		New mock = PowerMockito.mock(New.class);
		when(mock.getter()).thenReturn("mocked");
		PowerMockito.whenNew(New.class).withNoArguments().thenReturn(mock);

		String result = new New().getter();

		assertEquals("mocked", result);
		PowerMockito.verifyNew(New.class).withNoArguments();
	}

	@Test public void testAnnotations() {
		assertNotNull(finalMock);
	}
}
