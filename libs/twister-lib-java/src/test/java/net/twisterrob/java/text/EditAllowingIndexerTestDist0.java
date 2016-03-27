package net.twisterrob.java.text;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EditAllowingIndexerTestDist0 extends BaseIndexerTest {
	@Before public void setUp() throws Exception {
		index = new EditAllowingIndexer<>(0);
	}

	@Parameters(name = "{0}") public static Object[][] data() {
		return BaseIndexerTest.tests();
	}
}
