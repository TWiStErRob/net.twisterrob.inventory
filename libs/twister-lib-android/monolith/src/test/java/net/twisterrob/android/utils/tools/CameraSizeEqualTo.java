package net.twisterrob.android.utils.tools;

import java.util.Locale;

import org.hamcrest.*;

@SuppressWarnings("deprecation")
/*default*/ class CameraSizeEqualTo extends BaseMatcher<android.hardware.Camera.Size> {
	private final android.hardware.Camera.Size expected;
	public CameraSizeEqualTo(android.hardware.Camera.Size equalArg) {
		expected = equalArg;
	}
	@Override public boolean matches(Object item) {
		android.hardware.Camera.Size actual = (android.hardware.Camera.Size)item;
		return equals(expected, actual);
	}
	static boolean equals(android.hardware.Camera.Size expected, android.hardware.Camera.Size actual) {
		return expected.width == actual.width && expected.height == actual.height;
	}
	@Override public void describeTo(Description description) {
		description.appendText(toString(expected));
	}
	@Override public void describeMismatch(Object item, Description description) {
		description.appendText("was ").appendText(toString((android.hardware.Camera.Size)item));
	}
	private static String toString(android.hardware.Camera.Size size) {
		return String.format(Locale.ROOT, "%dx%d", size.width, size.height);
	}
	public static Matcher<android.hardware.Camera.Size> equalTo(android.hardware.Camera.Size operand) {
		return new CameraSizeEqualTo(operand);
	}
}
