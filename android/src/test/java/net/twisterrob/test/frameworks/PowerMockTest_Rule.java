package net.twisterrob.test.frameworks;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.junit.MockitoJUnit;

@RunWith(JUnit4.class)
@SuppressWarnings("JUnitTestCaseWithNoTests") // Inherited, testing those with this setup approach.
public class PowerMockTest_Rule extends PowerMockTests {

	@Rule public MethodRule power = MockitoJUnit.rule();

	@RunWith(JUnit4.class)
	public static class Inner extends PowerMockTests.Inner {

		@Rule public MethodRule power = MockitoJUnit.rule();
	}
}
