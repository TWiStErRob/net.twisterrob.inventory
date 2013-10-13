package net.twisterrob.java.model;
public class LocationUtils {
	protected static final double DELTA = 0.0001;
	public static boolean near(Location loc1, Location loc2) {
		return Math.abs(loc1.getLatitude() - loc2.getLatitude()) < DELTA
				&& Math.abs(loc1.getLongitude() - loc2.getLongitude()) < DELTA;
	}
}
