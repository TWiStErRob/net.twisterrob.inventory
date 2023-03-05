package net.twisterrob.inventory.database;

import org.junit.*;

import static org.junit.Assert.*;

import static net.twisterrob.inventory.database.LevelBasedID.*;

public class LevelBasedIDTest {
	private LevelBasedID level;

	@Before public void setUp() {
		level = new LevelBasedID();
	}

	@Test public void testLevel0() {
		for (int i = 1; i < MAX_PER_LEVEL * 2; i++) {
			assertEquals(i * 1000, level.newItem(0));
		}
	}
	@Test public void testLevel1() {
		assertEquals(1000, level.newItem(0));
		for (int i = 1; i < MAX_PER_LEVEL; i++) {
			assertEquals(1000 + i * (int)Math.pow(10, 2), level.newItem(1));
		}
		testFail(1);
		testFail(1);
	}
	@Test public void testLevel2() {
		assertEquals(1000, level.newItem(0));
		assertEquals(1100, level.newItem(1));
		for (int i = 1; i < MAX_PER_LEVEL; i++) {
			assertEquals(1100 + i * (int)Math.pow(10, 1), level.newItem(2));
		}
		testFail(2);
		testFail(2);
	}
	@Test public void testLevel3() {
		assertEquals(1000, level.newItem(0));
		assertEquals(1100, level.newItem(1));
		assertEquals(1110, level.newItem(2));
		for (int i = 1; i < MAX_PER_LEVEL; i++) {
			assertEquals(1110 + i * (int)Math.pow(10, 0), level.newItem(3));
		}
		testFail(3);
		testFail(3);
	}

	@Test public void testLevelExpanding() {
		assertEquals(1000, level.newItem(0));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(1));
		assertEquals(2200, level.newItem(1));
		assertEquals(2300, level.newItem(1));
		assertEquals(2310, level.newItem(2));
		assertEquals(2311, level.newItem(3));
		assertEquals(2312, level.newItem(3));
	}

	@Test(expected = IllegalStateException.class)
	public void testLevelExpandingTooQuick1_3() {
		assertEquals(1000, level.newItem(0));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(1));
		assertEquals(2200, level.newItem(1));
		level.newItem(3);
	}
	@Test(expected = IllegalStateException.class)
	public void testLevelExpandingTooQuick0_2() {
		assertEquals(1000, level.newItem(0));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(2));
		level.newItem(3);
	}
	@Test(expected = IllegalStateException.class)
	public void testLevelExpandingTooQuickWithoutParents() {
		level.newItem(2);
	}

	@Test public void testLevelShortening() {
		assertEquals(1000, level.newItem(0));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(1));
		assertEquals(2200, level.newItem(1));
		assertEquals(2300, level.newItem(1));
		assertEquals(2310, level.newItem(2));
		assertEquals(2311, level.newItem(3));
		assertEquals(2312, level.newItem(3)); // going back after this
		assertEquals(2320, level.newItem(2));
		assertEquals(2330, level.newItem(2));
		assertEquals(2400, level.newItem(1));
		assertEquals(2500, level.newItem(1));
		assertEquals(3000, level.newItem(0));
	}

	@Test public void testLevelShorteningQuick() {
		assertEquals(1000, level.newItem(0));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(1));
		assertEquals(2200, level.newItem(1));
		assertEquals(2210, level.newItem(2));
		assertEquals(2220, level.newItem(2));
		assertEquals(2221, level.newItem(3));
		assertEquals(2222, level.newItem(3));
		assertEquals(2300, level.newItem(1)); // skipped level 2 before here
		assertEquals(3000, level.newItem(0));
	}

	@Test public void testLevelFluctuationReal() {
		assertEquals(1000, level.newItem(0));
		assertEquals(1100, level.newItem(1));
		assertEquals(1110, level.newItem(2));
		assertEquals(1111, level.newItem(3));
		assertEquals(2000, level.newItem(0));
		assertEquals(2100, level.newItem(1));
		assertEquals(2200, level.newItem(1));
		assertEquals(2210, level.newItem(2));
		assertEquals(3000, level.newItem(0));
		assertEquals(3100, level.newItem(1));
		assertEquals(3110, level.newItem(2));
		assertEquals(3120, level.newItem(2));
		assertEquals(3200, level.newItem(1));
		assertEquals(3210, level.newItem(2));
		assertEquals(3211, level.newItem(3));
		assertEquals(3212, level.newItem(3));
		assertEquals(3220, level.newItem(2));
		assertEquals(3221, level.newItem(3));
		assertEquals(3300, level.newItem(1));
	}

	private void testFail(int level) {
		try {
			this.level.newItem(level);
			fail("Level " + level + " should fail after " + MAX_PER_LEVEL + " items.");
		} catch (IllegalStateException ex) {
			// good
		}
	}
}
