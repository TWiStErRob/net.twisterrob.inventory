package net.twisterrob.test.frameworks;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("JUnitTestCaseWithNoTests") // Inherited, testing those with this setup approach.
public class PowerMockTest_Runner extends PowerMockTests {

	@RunWith(MockitoJUnitRunner.class)
	public static class Inner extends PowerMockTests.Inner {
	}
}
