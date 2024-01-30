package net.twisterrob.test.frameworks;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("JUnitTestCaseWithNoTests") // Inherited, testing those with this setup approach.
public class PowerMockTest_Raw extends PowerMockTests {

	public static class Inner extends PowerMockTests.Inner {
		private AutoCloseable mocks;

		@Before
		public void setUp() {
			mocks = MockitoAnnotations.openMocks(this);
		}

		@After
		public void tearDown() throws Exception {
			mocks.close();
		}
	}
}
