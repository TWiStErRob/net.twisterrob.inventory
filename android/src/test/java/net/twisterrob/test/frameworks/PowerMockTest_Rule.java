package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.*;

import net.twisterrob.test.frameworks.classes.Final;

@RunWith(JUnit4.class)
//@PowerMockListener(AnnotationEnabler.class) // has no effect, not called
@SuppressWarnings("JUnitTestCaseWithNoTests") // inherited, testing those with this setup approach
public class PowerMockTest_Rule extends PowerMockTests {

	@Rule public PowerMockRule power = new PowerMockRule();

	@RunWith(JUnit4.class)
	public static class Inner extends PowerMockTests.Inner {

		@Rule public PowerMockRule power = new PowerMockRule();

		@Before public void setUp() {
			assertNull("@Mock doesn't work with rule based PowerMock 1.6.6", mockFinal);
			mockFinal = PowerMockito.mock(Final.class);
		}
	}
}
