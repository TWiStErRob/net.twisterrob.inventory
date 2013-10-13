package net.twisterrob.java.model;

import java.io.Serializable;

public class Location implements Serializable {
	private static final long serialVersionUID = 6864526975444885245L;

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

	@Override
	public String toString() {
		return String.format("%.6f, %.6f", m_longitude, m_latitude);
	}
}
