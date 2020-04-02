package net.twisterrob.android.utils.tools;

import java.util.*;

import org.hamcrest.*;

@SuppressWarnings("deprecation")
/*default*/ class CameraSizeListEqualTo extends BaseMatcher<List<android.hardware.Camera.Size>> {
	private final List<android.hardware.Camera.Size> expecteds;
	public CameraSizeListEqualTo(List<android.hardware.Camera.Size> equalArg) {
		expecteds = equalArg;
	}
	@Override public boolean matches(Object item) {
		List<android.hardware.Camera.Size> actuals = cast(item);
		if (expecteds.size() != actuals.size()) {
			return false;
		}
		for (int pos = 0; pos < expecteds.size(); pos++) {
			if (!CameraSizeEqualTo.equals(expecteds.get(pos), actuals.get(pos))) {
				return false;
			}
		}
		return true;
	}
	@Override public void describeTo(Description description) {
		appendText(description, expecteds);
	}
	private void appendText(Description description, List<android.hardware.Camera.Size> expecteds) {
		description.appendText("\n");
		for (android.hardware.Camera.Size size : expecteds) {
			float longer = size.width;
			float shorter = size.height;
			boolean land = size.width > size.height;
			if (longer < shorter) {
				float temp = shorter;
				shorter = longer;
				longer = temp;
			}
			description.appendText(String.format(Locale.ROOT, "%dx%d(land%s=%.3f, port%s=%.3f)%n",
					size.width, size.height,
					land? "*" : "", longer / shorter, land? "" : "*", shorter / longer));
		}
	}
	@Override public void describeMismatch(Object item, Description description) {
		List<android.hardware.Camera.Size> actuals = cast(item);
		description.appendText("was ");
		appendText(description, actuals);
		description.appendText("\n");
		description.appendText("     ");
	}
	@SuppressWarnings("unchecked")
	private static List<android.hardware.Camera.Size> cast(Object item) {
		return (List<android.hardware.Camera.Size>)item;
	}
	private static String toString(android.hardware.Camera.Size size) {
		return String.format(Locale.ROOT, "%dx%d", size.width, size.height);
	}
	public static Matcher<List<android.hardware.Camera.Size>> equalTo(List<android.hardware.Camera.Size> operand) {
		return new CameraSizeListEqualTo(operand);
	}
}
