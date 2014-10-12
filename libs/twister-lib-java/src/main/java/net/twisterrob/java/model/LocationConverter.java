package net.twisterrob.java.model;

// TODO https://groups.google.com/forum/#!topic/Google-Maps-API/6459F-hBMqc
// TODO http://www.uwgb.edu/dutchs/UsefulData/UTMFormulas.HTM
// TODO http://www.uwgb.edu/dutchs/UsefulData/ConvertUTMNoOZ.HTM
public class LocationConverter {
	public static net.twisterrob.java.model.Location gridRef2LatLon(int easting, int northing) {
		Location OSGB36 = MoveableTypeGridRefLocationConverter.f(easting, northing);
		return MoveableTypeGridRefLocationConverter.convertOSGB36toWGS84(OSGB36);
	}

	/** WGS84: a in meters*/
	private static final int WGS84_EARTH_EQI_RADIUS /* a */ = 6378137;
	/** WGS84: 1/f */
	private static final double INVERSE_FLATTENING /* 1/f */ = 298.257223563;
	/** WGS84: f = 1/(1/f) */
	private static final double FLATTENING /* f */ = 1 / INVERSE_FLATTENING;
	/** WGS84: e^2 = 2f - f^2 */
	private static final double ECCENTRICITY_SQUARED /* e^2 */ =
			2 * FLATTENING - FLATTENING * FLATTENING; // 0.00669437999014

	/**
	 * 2b: calculate E-W span in degrees: now more tricky: calculate like 2a,
	 * but now divide by cos(centerLatitude) to compensate that E-W distances need more degrees when moving north to have the same meters.
	 *
	 * @param phi Latitude in radians
	 * @return length of a longitude degree in meters
	 * @see <a href="http://stackoverflow.com/a/17011213/253468">Directions</a>
	 * @see <a href="http://en.wikipedia.org/wiki/Latitude#The_length_of_a_degree_of_latitude">Math</a>
	 */
	public static double metersPerDegreeLon(double phi) {
		return (Math.PI * WGS84_EARTH_EQI_RADIUS * Math.cos(phi))
				/ (180 * Math.sqrt(1 - ECCENTRICITY_SQUARED * Math.sin(phi) * Math.sin(phi)));
	}

	/**
	 * 2a: calculate north-south diameter of circle/ in degrees: this a bit tricky: the distance is define in meters,
	 * you need a transformation to get the latitudeSpan: one degrees of lat is approx 111.3 km (earth circumference / 360.0):
	 * With this meters_per_degree value calc the N-S distance in degrees.
	 *
	 * @param phi Latitude in radians
	 * @return length of a latitude degree in meters
	 * @see <a href="http://stackoverflow.com/a/17011213/253468">Directions</a>
	 * @see <a href="http://en.wikipedia.org/wiki/Latitude#The_length_of_a_degree_of_latitude">Math</a>
	 */
	public static double metersPerDegreeLat(double phi) {
		return 111132.954 - 559.822 * Math.cos(2 * phi) + 1.175 * Math.cos(4 * phi);
	}
}
