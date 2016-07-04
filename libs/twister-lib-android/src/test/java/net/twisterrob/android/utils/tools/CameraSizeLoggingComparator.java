package net.twisterrob.android.utils.tools;

import java.util.*;

@SuppressWarnings("deprecation")
/*default*/ class CameraSizeLoggingComparator implements Comparator<android.hardware.Camera.Size> {
	private final Comparator<android.hardware.Camera.Size> comparator;
	private CameraSizeLoggingComparator(Comparator<android.hardware.Camera.Size> comparator) {
		this.comparator = comparator;
	}
	@Override public int compare(android.hardware.Camera.Size lhs, android.hardware.Camera.Size rhs) {
		int result = comparator.compare(lhs, rhs);
		System.out.printf(Locale.ROOT, "%dx%d <> %dx%d: %d%n",
				lhs.width, lhs.height, rhs.width, rhs.height, result);
		return result;
	}
}
