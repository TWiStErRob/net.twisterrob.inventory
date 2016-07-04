package net.twisterrob.android.utils.tools;

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

import static net.twisterrob.android.utils.tools.CameraSizeHelper.*;

@SuppressWarnings("deprecation")
@RunWith(Parameterized.class)
public class CameraSizeComparatorTest {
	@SuppressWarnings({"unused", "FieldCanBeLocal"}) // useful for debugging a specific test
	private final String testName;
	private final int width;
	private final int height;
	private final android.hardware.Camera.Size lhs;
	private final android.hardware.Camera.Size rhs;

	public CameraSizeComparatorTest(String testName,
			int width, int height, int lhsWidth, int lhsHeight, int rhsWidth, int rhsHeight) {
		this.testName = testName;
		this.width = width;
		this.height = height;
		this.lhs = s(lhsWidth, lhsHeight);
		this.rhs = s(rhsWidth, rhsHeight);
	}

	@Parameters(name = "{0}: {1}x{2}.compare({3}x{4}, {5}x{6})")
	public static Object[][] data() {
		return new Object[][] { // Δ is the relative change to the comparator's ratio (actual - comp)
				// new Comparator   (   ,    ).compare(new Size   (   ,    ), new Size          (   ,    )) == -1 (<)
				{"Closer land- ratio wins",
						/*land 1.5*/ 150, 100, /*land 1.25 Δ-.25*/ 125, 100, /*land 1.15 Δ-.35*/ 115, 100},
				{"Closer land+ ratio wins",
						/*land 1.5*/ 150, 100, /*land 1.65 Δ+.15*/ 165, 100, /*land 1.75 Δ+.25*/ 175, 100},
				{"Closer land-+ ratio wins",
						/*land 1.5*/ 150, 100, /*land 1.45 Δ-.05*/ 145, 100, /*land 1.65 Δ+.15*/ 165, 100},
				{"Closer land+- ratio wins",
						/*land 1.5*/ 150, 100, /*land 1.65 Δ+.15*/ 165, 100, /*land 1.25 Δ-.25*/ 125, 100},
				{"Closer port- ratio wins",
						/*port 1.5*/ 100, 150, /*port 1.25 Δ-.25*/ 100, 125, /*port 1.15 Δ-.35*/ 100, 115},
				{"Closer port+ ratio wins",
						/*port 1.5*/ 100, 150, /*port 1.65 Δ+.15*/ 100, 165, /*port 1.75 Δ+.25*/ 100, 175},
				{"Closer port-+ ratio wins",
						/*port 1.5*/ 100, 150, /*port 1.45 Δ-.05*/ 100, 145, /*port 1.65 Δ+.15*/ 100, 165},
				{"Closer port+- ratio wins",
						/*port 1.5*/ 100, 150, /*port 1.65 Δ+.15*/ 100, 165, /*port 1.25 Δ-.25*/ 100, 125},
				{"Land+ ratio VS port+ ratio",
						/*land 1.5*/ 150, 100, /*land 1.65 Δ+.15*/ 165, 100, /*port 1.75 Δ+.25*/ 100, 175},
				{"Land+ ratio VS port- ratio",
						/*land 1.5*/ 150, 100, /*land 1.65 Δ+.15*/ 135, 100, /*port 1.25 Δ-.25*/ 100, 125},
				{"Land- ratio VS port+ ratio",
						/*land 1.5*/ 150, 100, /*land 1.35 Δ-.15*/ 135, 100, /*port 1.75 Δ+.25*/ 100, 175},
				{"Land- ratio VS port- ratio",
						/*land 1.5*/ 150, 100, /*land 1.35 Δ-.15*/ 135, 100, /*port 1.25 Δ-.25*/ 100, 125},
				{"Same ratio delta, larger area wins",
						/*land 1.5*/ 150, 100, /*land 1.75 Δ+.25*/ 175, 100, /*land 1.25 Δ-.25*/ 125, 100},
				{"Same ratio delta, larger area wins, even if different orientation",
						/*land 1.5*/ 150, 100, /*port 1.75 Δ+.25*/ 100, 175, /*land 1.25 Δ-.25*/ 125, 100},
				{"Same ratio delta, in same direction, larger area wins",
						/*land 1.5*/ 150, 100, /*land 1.75 Δ+.25*/ 350, 200, /*land 1.75 Δ+.25*/ 175, 100},
				{"Same ratio delta, same area, better orientation (land) wins",
						/*land 1.5*/ 150, 100, /*land 1.75 Δ+.25*/ 175, 100, /*port 1.75 Δ+.25*/ 100, 175},
				{"Same ratio delta, same area, better orientation (port) wins",
						/*port 1.5*/ 100, 150, /*port 1.75 Δ+.25*/ 100, 175, /*land 1.75 Δ+.25*/ 175, 100},
		};
	}

	@Test public void testLessThan() {
		test(lhs, rhs, -1);
	}
	@Test public void testGreaterThan() {
		test(rhs, lhs, 1);
	}
	@Test public void testEquals() {
		test(lhs, lhs, 0);
		test(rhs, rhs, 0);
	}

	private void test(android.hardware.Camera.Size lhs, android.hardware.Camera.Size rhs, int expected) {
		Comparator<android.hardware.Camera.Size> comparator = new CameraSizeComparator(width, height);
		int result = comparator.compare(lhs, rhs);
		String message = String.format(Locale.ROOT, "Expected %dx%d %s %dx%d, but got %s",
				lhs.width, lhs.height, toRelation(expected), rhs.width, rhs.height, toRelation(result));
		assertEquals(message, Integer.signum(expected), Integer.signum(result));
	}
	private static String toRelation(int result) {
		switch (Integer.signum(result)) {
			case -1:
				return "<";
			case +1:
				return ">";
			case 0:
				return "=";
			default:
				throw new InternalError("Signum implementation failed");
		}
	}
}
