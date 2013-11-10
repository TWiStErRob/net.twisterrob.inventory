package net.twisterrob.java.model;

import static java.lang.Math.*;

public class LocationUtils {
	protected static final double DELTA = 0.0001;
	public static boolean near(Location loc1, Location loc2) {
		return abs(loc1.getLatitude() - loc2.getLatitude()) < DELTA
				&& abs(loc1.getLongitude() - loc2.getLongitude()) < DELTA;
	}
	// http://en.wikipedia.org/wiki/Great-circle_distance
	// Geodetic coordinates, m distance, using Vincenty formula
	/**
	 * @return meters distance
	 */
	public static double distance(Location p1, Location p2) {
		double meanRadius = (earthRadius(p1.getLatitude()) + earthRadius(p2.getLatitude())) / 2.0 * 1000;
		return meanRadius
				* vincenty(toRadians(p1.getLatitude()), toRadians(p1.getLongitude()), toRadians(p2.getLatitude()),
						toRadians(p2.getLongitude()));
	}
	private static double vincenty(double phi_s, double lambda_s, double phi_f, double lambda_f) {
		double delta_lambda = lambda_s - lambda_f;
		return atan2(
				sqrt(pow(cos(phi_f) * sin(delta_lambda), 2)
						+ pow(cos(phi_s) * sin(phi_f) - sin(phi_s) * cos(phi_f) * cos(delta_lambda), 2)), sin(phi_s)
						* sin(phi_f) + cos(phi_s) * cos(phi_f) * cos(delta_lambda));
	}

	private static final double equatorial_radius = 6378.1370;
	private static final double polar_radius = 6356.752314245; // often rounded to 6356.7523
	// http://en.wikipedia.org/wiki/Earth_radius
	// http://upload.wikimedia.org/0/c/d/0cd950795690e08f0d5accfee3291669.png
	// Constants: http://en.wikipedia.org/wiki/WGS-84
	/**
	 * @return km radius
	 */
	public static double earthRadius(double latitude) {
		double nominator = pow(pow(equatorial_radius, 2) * cos(latitude), 2)
				+ pow(pow(polar_radius, 2) * sin(latitude), 2);
		double denominator = pow(equatorial_radius * cos(latitude), 2) + pow(polar_radius * sin(latitude), 2);
		return sqrt(nominator / denominator);
	}
}
