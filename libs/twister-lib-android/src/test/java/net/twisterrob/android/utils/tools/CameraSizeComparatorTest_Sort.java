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
public class CameraSizeComparatorTest_Sort {
	private static final boolean PRINT_ALL = true;
	private final int width;
	private final int height;
	private final List<android.hardware.Camera.Size> expected;

	public CameraSizeComparatorTest_Sort(@SuppressWarnings("unused") String name,
			int width, int height, List<android.hardware.Camera.Size> expected) {
		this.width = width;
		this.height = height;
		this.expected = expected;
	}

	@Parameters(name = "{0}")
	public static Object[][] data() {
		return new Object[][] {
				{"Same aspect, Different sizes", 1500, 1000,
						Arrays.asList(s(3000, 2000), s(2000, 3000), s(750, 500), s(500, 750))},
				{"Different aspects (larger)", 150, 100,
						Arrays.asList(s(175, 100), s(200, 100), s(250, 100), s(300, 100))},
				{"Different aspects (smaller)", 150, 100,
						Arrays.asList(s(133, 100), s(125, 100), s(115, 100), s(100, 100))},
				{"Similar aspects (larger + smaller)", 150, 100,
						Arrays.asList(s(133, 100), s(175, 100), s(125, 100), s(115, 100),
								s(200, 100), s(100, 100), s(250, 100), s(300, 100))},
		};
	}

	@Test public void test() {
		List<android.hardware.Camera.Size> actual = new ArrayList<>(expected);
		Collections.shuffle(actual);

		Comparator<android.hardware.Camera.Size> comparator = new CameraSizeComparator(width, height);
		Collections.sort(actual, comparator);

		if (PRINT_ALL) {
			System.out.printf(Locale.ROOT, "Screen %dx%d (%.3f)%n", width, height, width / (float)height);
			System.out.println("Expected:");
			printSizes(expected);
		}

		try {
			assertSorted(actual, comparator);
			assertSameAs(expected, actual);
		} catch (Throwable t) { // print actual values if something failed
			if (PRINT_ALL) {
				System.out.println();
				System.out.println("Actual:");
				printSizes(actual);
			}
			throw t;
		}
	}

//	@Ignore
//	@Test public void test1() {
//		ArrayList<Size> sizes = new ArrayList<>(Arrays.asList(
//				s(1000, 1000), s(2000, 1000), s(1000, 2000), s(1500, 1000), s(1000, 1500)
//		));
//		int count = sizes.size();
//		for (int i = 0; i < count; i++) {
//			Size size = sizes.get(i);
//			sizes.add(s(size.width / 2, size.height / 2));
//			sizes.add(s(size.width * 2, size.height * 2));
//			sizes.add(s(size.width / 2, size.height));
//			sizes.add(s(size.width, size.height / 2));
//			sizes.add(s(size.width * 2, size.height));
//			sizes.add(s(size.width, size.height * 2));
//		}
//	}

	private static void assertSameAs(
			List<android.hardware.Camera.Size> expecteds, List<android.hardware.Camera.Size> actuals) {
		assertThat(actuals, CameraSizeListEqualTo.equalTo(expecteds));
//		assertEquals(expecteds.size(), actuals.size());
//		for (int pos = 0; pos < expecteds.size(); pos++) {
//			assertThat("At position " + pos, expecteds.get(pos), equalTo(actuals.get(pos)));
//		}
	}

	private static void assertSorted(
			List<android.hardware.Camera.Size> sizes, Comparator<android.hardware.Camera.Size> comparator) {
		for (int i = 1; i < sizes.size(); i++) {
			android.hardware.Camera.Size prev = sizes.get(i - 1);
			android.hardware.Camera.Size curr = sizes.get(i);
			if (comparator.compare(prev, curr) > 0) {
				printSizes(sizes);
				fail(String.format(Locale.ROOT, "Invalid order at %d between %dx%d and %dx%d",
						i, prev.width, prev.height, curr.width, curr.height));
			}
		}
	}
}
