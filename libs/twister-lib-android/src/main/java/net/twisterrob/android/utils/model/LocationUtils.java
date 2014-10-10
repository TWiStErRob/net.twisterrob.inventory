package net.twisterrob.android.utils.model;

import com.google.android.gms.maps.model.LatLng;

import net.twisterrob.java.model.Location;

public class LocationUtils extends net.twisterrob.java.model.LocationUtils {

	public static LatLng toLatLng(Location loc) {
		return new LatLng(loc.getLatitude(), loc.getLongitude());
	}

	public static Location fromLatLng(LatLng ll) {
		return new Location(ll.latitude, ll.longitude);
	}

	public static boolean near(LatLng ll, Location loc) {
		return near(loc, ll);
	}

	public static boolean near(Location loc, LatLng ll) {
		return Math.abs(ll.latitude - loc.getLatitude()) < DELTA && Math.abs(ll.longitude - loc.getLongitude()) < DELTA;
	}
}
