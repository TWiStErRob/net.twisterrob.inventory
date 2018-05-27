package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class JUnit4Test {
	@Rule public TestName name = new TestName();

	@Test public void testRunner() {
		assertTrue("@Test methods should be executed", true);
	}

	@Test public void testRuleExecuted() {
		assertThat(name.getMethodName(), is("testRuleExecuted"));
	}
}
