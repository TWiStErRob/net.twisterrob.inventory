package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.*;

import net.twisterrob.test.frameworks.classes.Final;

@RunWith(JUnit4.class)
@PowerMockIgnore({"org.mockito.*", "org.powermock.*", "org.junit.*", "com.thoughtworks.xstream.*", "org.gradle.*",
		                 "java.*", "org.fusesource.jansi.*", "org.w3c.*", "javax.*"})
@PrepareForTest({Final.class})
//@PowerMockListener(AnnotationEnabler.class) // has no effect, not called
public class PowerMockTest_Rule {
	@Rule public PowerMockRule power = new PowerMockRule();
	/*@Mock*/ Final finalMock;

	@Before public void setUp() {
		finalMock = PowerMockito.mock(Final.class); // @Mock doesn't work
	}

	@Test public void testAnnotations() {
		assertNotNull(finalMock);
	}

	/** Check that final classes/methods can be mocked. */
	@Test public void testFinal() {
		PowerMockito.when(finalMock.finalMethod()).thenReturn("mocked");

		String result = finalMock.finalMethod();

		assertEquals("mocked", result);
	}
}
