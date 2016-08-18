package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class JUnitTest {
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
