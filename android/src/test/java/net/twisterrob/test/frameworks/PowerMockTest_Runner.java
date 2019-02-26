package net.twisterrob.test.frameworks;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@SuppressWarnings("JUnitTestCaseWithNoTests") // inherited, testing those with this setup approach
public class PowerMockTest_Runner extends PowerMockTests {

	@RunWith(PowerMockRunner.class)
	public static class Inner extends PowerMockTests.Inner {
	}
}
