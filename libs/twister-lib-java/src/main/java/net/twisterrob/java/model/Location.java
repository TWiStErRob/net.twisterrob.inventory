package net.twisterrob.java.model;

import java.io.Serializable;
import java.util.Locale;

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

	public Location diff(Location loc) {
		return new Location(this.m_latitude - loc.m_latitude, this.m_longitude - loc.m_longitude);
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%.10f, %.10f", m_latitude, m_longitude);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(m_latitude);
		result = prime * result + (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m_longitude);
		result = prime * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Location)) {
			return false;
		}
		Location other = (Location)obj;
		return Double.doubleToLongBits(m_latitude) == Double.doubleToLongBits(other.m_latitude)
				&& Double.doubleToLongBits(m_longitude) == Double.doubleToLongBits(other.m_longitude);
	}
}
