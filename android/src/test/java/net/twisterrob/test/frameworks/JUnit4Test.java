package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class JUnit4Test {
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Test public void testRunner() {
		assertTrue(true);
	}

	@Test public void testException() {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("test");
		throw new NullPointerException("test");
	}
}
