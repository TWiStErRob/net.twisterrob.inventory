package net.twisterrob.android.utils.model;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;

public class Location implements Serializable {
	private static final long serialVersionUID = 6864526975444885245L;

	private static final double GEO = 1e6;
	private static final double DELTA = 0.0001;

	private final double m_latitude;
	private final double m_longitude;

	public Location(final double latitude, final double longitude) {
		m_latitude = latitude;
		m_longitude = longitude;
	}

	public double getLatitude() {
		return m_latitude;
	}

	public double getLongitude() {
		return m_longitude;
	}

	public GeoPoint toGeoPoint() {
		return new GeoPoint((int)(m_latitude * GEO), (int)(m_longitude * GEO));
	}

	public static Location fromGeoPoint(final GeoPoint geo) {
		return new Location(geo.getLatitudeE6() / GEO, geo.getLongitudeE6() / GEO);
	}

	public boolean near(final GeoPoint geo) {
		return Math.abs(geo.getLatitudeE6() / GEO - m_latitude) < DELTA
				&& Math.abs(geo.getLongitudeE6() / GEO - m_longitude) < DELTA;
	}

	public boolean near(final Location other) {
		return Math.abs(other.m_latitude - this.m_latitude) < DELTA
				&& Math.abs(other.m_longitude - this.m_longitude) < DELTA;
	}

	@Override
	public String toString() {
		return String.format("%.6f, %.6f", m_latitude, m_longitude);
	}
}
