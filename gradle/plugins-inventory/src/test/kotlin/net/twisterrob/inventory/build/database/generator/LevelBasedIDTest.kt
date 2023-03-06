package net.twisterrob.inventory.build.database.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlin.math.pow

class LevelBasedIDTest {

	private lateinit var level: LevelBasedID

	@Before fun setUp() {
		level = LevelBasedID()
	}

	@Test fun testLevel0() {
		for (i in 1 until LevelBasedID.MAX_PER_LEVEL * 2) {
			assertEquals(i * 1000, level.newItem(0))
		}
	}

	@Test fun testLevel1() {
		assertEquals(1000, level.newItem(0))
		for (i in 1 until LevelBasedID.MAX_PER_LEVEL) {
			assertEquals(1000 + i * 10.0.pow(2).toInt(), level.newItem(1))
		}
		testFail(1)
		testFail(1)
	}

	@Test fun testLevel2() {
		assertEquals(1000, level.newItem(0))
		assertEquals(1100, level.newItem(1))
		for (i in 1 until LevelBasedID.MAX_PER_LEVEL) {
			assertEquals(1100 + i * 10.0.pow(1).toInt(), level.newItem(2))
		}
		testFail(2)
		testFail(2)
	}

	@Test fun testLevel3() {
		assertEquals(1000, level.newItem(0))
		assertEquals(1100, level.newItem(1))
		assertEquals(1110, level.newItem(2))
		for (i in 1 until LevelBasedID.MAX_PER_LEVEL) {
			assertEquals(1110 + i * 10.0.pow(0).toInt(), level.newItem(3))
		}
		testFail(3)
		testFail(3)
	}

	@Test fun testLevelExpanding() {
		assertEquals(1000, level.newItem(0))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(1))
		assertEquals(2200, level.newItem(1))
		assertEquals(2300, level.newItem(1))
		assertEquals(2310, level.newItem(2))
		assertEquals(2311, level.newItem(3))
		assertEquals(2312, level.newItem(3))
	}

	@Test(expected = IllegalStateException::class)
	fun testLevelExpandingTooQuick1_3() {
		assertEquals(1000, level.newItem(0))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(1))
		assertEquals(2200, level.newItem(1))
		level.newItem(3)
	}

	@Test(expected = IllegalStateException::class)
	fun testLevelExpandingTooQuick0_2() {
		assertEquals(1000, level.newItem(0))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(2))
		level.newItem(3)
	}

	@Test(expected = IllegalStateException::class)
	fun testLevelExpandingTooQuickWithoutParents() {
		level.newItem(2)
	}

	@Test fun testLevelShortening() {
		assertEquals(1000, level.newItem(0))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(1))
		assertEquals(2200, level.newItem(1))
		assertEquals(2300, level.newItem(1))
		assertEquals(2310, level.newItem(2))
		assertEquals(2311, level.newItem(3))
		assertEquals(2312, level.newItem(3)) // going back after this
		assertEquals(2320, level.newItem(2))
		assertEquals(2330, level.newItem(2))
		assertEquals(2400, level.newItem(1))
		assertEquals(2500, level.newItem(1))
		assertEquals(3000, level.newItem(0))
	}

	@Test fun testLevelShorteningQuick() {
		assertEquals(1000, level.newItem(0))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(1))
		assertEquals(2200, level.newItem(1))
		assertEquals(2210, level.newItem(2))
		assertEquals(2220, level.newItem(2))
		assertEquals(2221, level.newItem(3))
		assertEquals(2222, level.newItem(3))
		assertEquals(2300, level.newItem(1)) // skipped level 2 before here
		assertEquals(3000, level.newItem(0))
	}

	@Test fun testLevelFluctuationReal() {
		assertEquals(1000, level.newItem(0))
		assertEquals(1100, level.newItem(1))
		assertEquals(1110, level.newItem(2))
		assertEquals(1111, level.newItem(3))
		assertEquals(2000, level.newItem(0))
		assertEquals(2100, level.newItem(1))
		assertEquals(2200, level.newItem(1))
		assertEquals(2210, level.newItem(2))
		assertEquals(3000, level.newItem(0))
		assertEquals(3100, level.newItem(1))
		assertEquals(3110, level.newItem(2))
		assertEquals(3120, level.newItem(2))
		assertEquals(3200, level.newItem(1))
		assertEquals(3210, level.newItem(2))
		assertEquals(3211, level.newItem(3))
		assertEquals(3212, level.newItem(3))
		assertEquals(3220, level.newItem(2))
		assertEquals(3221, level.newItem(3))
		assertEquals(3300, level.newItem(1))
	}

	private fun testFail(level: Int) {
		try {
			this.level.newItem(level)
			fail("Level " + level + " should fail after " + LevelBasedID.MAX_PER_LEVEL + " items.")
		} catch (ex: IllegalStateException) {
			// good
		}
	}
}
