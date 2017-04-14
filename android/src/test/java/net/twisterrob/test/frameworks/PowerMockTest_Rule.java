package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.*;

import net.twisterrob.test.frameworks.classes.Final;

//@PowerMockIgnore({"org.mockito.*","org.powermock.api.mockito.repackaged.*"})
@RunWith(JUnit4.class)
//@PowerMockIgnore({"org.mockito.*", "org.powermock.*", "org.junit.*", "com.thoughtworks.xstream.*", "org.gradle.*",
//		"java.*", "org.fusesource.jansi.*", "org.w3c.*", "javax.*"})
@PrepareForTest({Final.class})
//@PowerMockListener(AnnotationEnabler.class) // has no effect, not called
public class PowerMockTest_Rule {
	@Rule public PowerMockRule power = new PowerMockRule();

	@Mock Final finalMock;

	@Before public void setUp() {
		assertNull("@Mock doesn't work with rule based PowerMock 1.6.6", finalMock);
		finalMock = PowerMockito.mock(Final.class);
	}

	@Test public void testAnnotations() {
		assertNotNull(finalMock);
		assertTrue(Mockito.mockingDetails(finalMock).isMock());
	}

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinalDoReturn() {
		PowerMockito.doReturn("mocked").when(finalMock).finalMethod();

		String result = finalMock.finalMethod();

		assertEquals("mocked", result);
	}

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinalWhenThenReturn() {
		PowerMockito.when(finalMock.finalMethod()).thenReturn("mocked");

		String result = finalMock.finalMethod();

		assertEquals("mocked", result);
	}
}
