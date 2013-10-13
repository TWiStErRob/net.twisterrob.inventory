package net.twisterrob.java.model;
/**
 * This code is based on {@link http://osgbwebmaptools.cvs.sourceforge.net/viewvc/osgbwebmaptools/OSGBWebMapTools/OSGBWebMapTools-API/lib/OSGBWebMapTools/GridProjection.js?revision=1.1&view=markup}.
 */
@SuppressWarnings("unused")
public class LocationConverter {
	private static final boolean debug = false;
	private static final double DEG_TO_RAD = Math.PI / 180.0;
	private static final double a = 6378137.0; /* semi-major axis */
	private static final double b = 6356752.3141; /* semi-minor axis */
	private static final double f = 1.0 - b / a; /* flattening */
	private static final double e2 = 1.0 - (b * b) / (a * a); /* eccentricity squared */
	private static final double lat0 = 49.0 * DEG_TO_RAD; /* latitude of origin, in radians */
	private static final double lon0 = -2.0 * DEG_TO_RAD; /* longitude of origin, in radians */
	private static final double falseE = 400000.0; /* false origin eastings */
	private static final double falseN = -100000.0; /* false origin northings */
	private static final double scl = 0.9996012717; /* scale factor */
	private static final double ety = (a - b) / (a + b); /* ellipticity */
	private static final double aS = a * scl; /* scaled major_axis */
	private static final double bS = b * scl; /* scaled minor axis */

	/**
	 * Convert an ETRS89 point into a WGS84 point
	 * @param easting pt_ETRS89 - {@code <OpenLayers.LonLat>} The point to convert
	 * @param northing pt_ETRS89 - {@code <OpenLayers.LonLat>} The point to convert
	 * @return {<OpenLayers.LonLat>} The point converted into WGS84
	 * @see E_N_to_LonLat E_N_to_LonLat from JS source
	 */
	public static Location E_N_to_LonLat(double easting, double northing) {
		// Un-project Transverse Mercator eastings and northings back to latitude.
		// Input:
		//   eastings (East) and northings (North) in meters;
		//   ellipsoid axis dimensions (a & b) in meters;
		//   eastings (e0) and northings (n0) of false origin in meters;
		//   central meridian scale factor (f0) and
		//   latitude (PHI0) and longitude (LAM0) of false origin in decimal degrees.

		// REQUIRES THE "Marc" AND "InitialLat" FUNCTIONS

		// Convert angle measures to radians
		double RadPHI0 = lat0;
		double RadLAM0 = lon0;

		// Compute af0, bf0, e squared (e2), n and Et
		double af0 = a * scl;
		double bf0 = b * scl;
		double e2 = ((af0 * af0) - (bf0 * bf0)) / (af0 * af0);
		double n = (af0 - bf0) / (af0 + bf0);
		double Et = easting - falseE;

		if (debug) {
			System.out.println("af0: " + af0);
			System.out.println("bf0: " + bf0);
			System.out.println("e2: " + e2);
			System.out.println("Et: " + Et);
		}

		// Compute initial value for latitude (PHI) in radians
		double PHId = initialLat(northing, falseN, af0, RadPHI0, n, bf0);

		// Compute nu, rho and eta2 using value for PHId
		double sinPHId = Math.sin(PHId);
		double sinPHId2 = sinPHId * sinPHId;
		double nu = af0 / (Math.sqrt(1.0 - (e2 * sinPHId2)));
		double rho = (nu * (1.0 - e2)) / (1.0 - (e2 * sinPHId2));
		double eta2 = (nu / rho) - 1.0;

		if (debug) {
			System.out.println("PHId: " + PHId);
			System.out.println("nu: " + nu);
			System.out.println("rho: " + rho);
			System.out.println("eta2: " + eta2);
		}

		// Compute Latitude
		double tanPHId = Math.tan(PHId);
		double tanPHId2 = tanPHId * tanPHId;
		double tanPHId4 = tanPHId2 * tanPHId2;
		double tanPHId6 = tanPHId4 * tanPHId2;
		double VII = (tanPHId) / (2 * rho * nu);
		double VIII = (tanPHId / (24 * rho * (nu * nu * nu))) * (5 + (3 * tanPHId2) + eta2 - (9 * eta2 * tanPHId2));
		double IX = (tanPHId / (720 * rho * (nu * nu * nu * nu * nu))) * (61 + (90 * tanPHId2) + (45 * tanPHId4));
		double E_N_to_Lat = (180 / Math.PI)
				* (PHId - ((Et * Et) * VII) + ((Et * Et * Et * Et) * VIII) - ((Et * Et * Et * Et * Et * Et) * IX));

		// Compute Longitude
		double cosPHId = Math.cos(PHId);
		double cosPHId_1 = 1.0 / cosPHId;
		double X = cosPHId_1 / nu;
		double XI = (cosPHId_1 / (6 * (nu * nu * nu))) * ((nu / rho) + (2 * tanPHId2));
		double XII = (cosPHId_1 / (120 * (nu * nu * nu * nu * nu))) * (5 + (28 * tanPHId2 + (24 * tanPHId4)));
		double XIIA = (cosPHId_1 / (5040 * (nu * nu * nu * nu * nu * nu * nu)))
				* (61 + (662 * tanPHId2 + (1320 * tanPHId4 + (720 * tanPHId6))));

		if (debug) {
			System.out.println("VII: " + VII);
			System.out.println("VIII: " + VIII);
			System.out.println("IX: " + IX);
			System.out.println("X: " + X);
			System.out.println("XI: " + XI);
			System.out.println("XII: " + XII);
			System.out.println("XIIA: " + XIIA);
		}
		double E_N_to_Lng = (180 / Math.PI)
				* (RadLAM0 + (Et * X) - ((Et * Et * Et) * XI) + ((Et * Et * Et * Et * Et) * XII) - ((Et * Et * Et * Et
						* Et * Et * Et) * XIIA));
		Location pt_LonLat = new Location(E_N_to_Lat, E_N_to_Lng);

		if (debug) {
			System.out.println("E_N_to_Lat: " + E_N_to_Lat);
			System.out.println("E_N_to_Lon: " + E_N_to_Lng);
		}

		return pt_LonLat;
	}
	/**
	 * Method: initialLat
	 * Internal conversion method
	 * 
	 * Parameters:
	 * 
	 * Returns:
	 * {Float}
	 */
	private static double initialLat(double North, double n0, double afo, double PHI0, double n, double bfo) {
		// Compute initial value for Latitude (PHI) IN RADIANS.
		// Input:
		//   northing of point (North) and northing of false origin (n0) in meters;
		//   semi major axis multiplied by central meridian scale factor (af0) in meters;
		//   latitude of false origin (PHI0) IN RADIANS;
		//   n (computed from a, b and f0) and
		//   ellipsoid semi major axis multiplied by central meridian scale factor (bf0) in meters.
		// REQUIRES THE "Marc" FUNCTION
		// First PHI value (PHI1)
		double PHI1 = ((North - n0) / afo) + PHI0;
		// Calculate M
		double M = marc(bfo, n, PHI0, PHI1);
		// Calculate new PHI value (PHI2)
		double PHI2 = ((North - n0 - M) / afo) + PHI1;
		if (debug) {
			System.out.println("PHI1: " + PHI1);
			System.out.println("M: " + M);
			System.out.println("PHI2: " + PHI2);
		}
		// Iterate to get final value for InitialLat
		while (Math.abs(North - n0 - M) > 0.000001) {
			PHI2 = ((North - n0 - M) / afo) + PHI1;
			M = marc(bfo, n, PHI0, PHI2);
			if (debug) {
				System.out.println("PHI2: " + PHI2);
				System.out.println("M: " + M);
			}
			PHI1 = PHI2;
		}
		return PHI2;
	}
	/**
	 * Method: marc
	 * Internal conversion method to Compute meridional arc.
	 * 
	 * Parameters:
	 * 
	 * Returns:
	 * {Float}
	 */
	private static double marc(double bf0, double n, double PHI0, double PHI) {
		// Compute meridional arc.
		// Input: -
		//   ellipsoid semi major axis multiplied by central meridian scale factor (bf0) in meters;
		//   n (computed from a, b and f0);
		//   lat of false origin (PHI0) and initial or final latitude of point (PHI) IN RADIANS.
		// THIS FUNCTION IS CALLED BY THE - _
		// "Lat_Long_to_North" and "InitialLat" FUNCTIONS
		// THIS FUNCTION IS ALSO USED ON IT'S OWN IN THE "Projection and Transformation Calculations.xls" SPREADSHEET
		double marc = bf0
				* (((1.0 + n + ((5.0 / 4.0) * (n * n)) + ((5.0 / 4.0) * (n * n * n))) * (PHI - PHI0))
						- (((3.0 * n) + (3.0 * (n * n)) + ((21.0 / 8.0) * (n * n * n))) * Math.sin(PHI - PHI0) * Math
								.cos(PHI + PHI0))
						+ ((((15.0 / 8.0) * (n * n)) + ((15.0 / 8.0) * (n * n * n))) * Math.sin(2.0 * (PHI - PHI0)) * Math
								.cos(2.0 * (PHI + PHI0))) - (((35.0 / 24.0) * (n * n * n))
						* Math.sin(3.0 * (PHI - PHI0)) * Math.cos(3.0 * (PHI + PHI0))));
		return marc;
	}

}
