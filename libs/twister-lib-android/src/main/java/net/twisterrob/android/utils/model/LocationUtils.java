package net.twisterrob.android.utils.model;

import net.twisterrob.java.model.Location;

import com.google.android.maps.GeoPoint;

public class LocationUtils extends net.twisterrob.java.model.LocationUtils {
	private static final double GEO = 1e6d;

	public static GeoPoint toGeoPoint(Location loc) {
		return new GeoPoint((int)(loc.getLatitude() * GEO), (int)(loc.getLongitude() * GEO));
	}

	public static Location fromGeoPoint(GeoPoint geo) {
		return new Location(geo.getLatitudeE6() / GEO, geo.getLongitudeE6() / GEO);
	}

	public static boolean near(GeoPoint geo, Location loc) {
		return near(loc, geo);
	}
	public static boolean near(Location loc, GeoPoint geo) {
		return Math.abs(geo.getLatitudeE6() / GEO - loc.getLatitude()) < DELTA
				&& Math.abs(geo.getLongitudeE6() / GEO - loc.getLongitude()) < DELTA;
	}
}
