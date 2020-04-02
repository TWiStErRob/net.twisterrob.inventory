package net.twisterrob.android.utils.tools;

import java.util.Comparator;

import static java.lang.Math.*;

@SuppressWarnings("deprecation") 
/*default*/ class CameraSizeComparator implements Comparator<android.hardware.Camera.Size> {
	private final int screenWidth;
	private final int screenHeight;
	private final float screenRatio;
	public CameraSizeComparator(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.screenRatio = landscapeRatio(screenWidth, screenHeight);
	}
	@Override public int compare(android.hardware.Camera.Size lhs, android.hardware.Camera.Size rhs) {
		// try to compare with orientation-less ratio
		float lRatio = landscapeRatio(lhs.width, lhs.height);
		float rRatio = landscapeRatio(rhs.width, rhs.height);
		int compareRatio = Float.compare(abs(screenRatio - lRatio), abs(screenRatio - rRatio));
		if (compareRatio != 0) {
			return compareRatio; // closer to reference ratio wins
		}
		// Here: their aspect ratios are the same distance away from the reference

		// try the one whose area is larger
		long area = screenWidth * screenHeight;
		long lArea = lhs.width * lhs.height;
		long rArea = rhs.width * rhs.height;
		if (lArea >= area ^ rArea >= area) {
			return lArea >= area? -1 : +1; // larger than reference areas first 
		}
		// Here: either both areas are smaller than reference or both are bigger than reference 
		if (lArea != rArea) {
			return lArea > rArea? -1 : +1; // bigger area wins
		}
		// Here: their area is the same

		// try the one that's better oriented
		boolean lLand = lhs.width >= lhs.height;
		boolean rLand = rhs.width >= rhs.height;
		if (lLand != rLand) {
			boolean land = screenWidth >= screenHeight;
			return land == lLand? -1 : +1; // better oriented wins
		}
		// Here: they're both oriented the same way (not necessarily the same as reference's orientation)

		return 0;
	}

	private static float landscapeRatio(int width, int height) {
		return max(width / (float)height, height / (float)width);
	}
}
