package net.twisterrob.java.model;

import java.util.*;

/**
 * from http://www.movable-type.co.uk/scripts/latlong-gridref.html
 * from http://yetanotheruser.blogspot.co.uk/2007/01/converting-os-coodinates-to-longitude.html
 */
public class MoveableTypeGridRefLocationConverter {
	public static Location f(double E, double N) {
		double a = 6377563.396, b = 6356256.910;              // Airy 1830 major & minor semi-axes
		double F0 = 0.9996012717;                             // NatGrid scale factor on central meridian
		double lat0 = 49 * Math.PI / 180, lon0 = -2 * Math.PI / 180;  // NatGrid true origin
		double N0 = -100000, E0 = 400000;                     // northing & easting of true origin, metres
		double e2 = 1 - (b * b) / (a * a);                          // eccentricity squared
		double n = (a - b) / (a + b), n2 = n * n, n3 = n * n * n;

		double lat = lat0, M = 0;
		do {
			lat = (N - N0 - M) / (a * F0) + lat;

			double Ma = (1 + n + (5 / 4) * n2 + (5 / 4) * n3) * (lat - lat0);
			double Mb = (3 * n + 3 * n * n + (21 / 8) * n3) * Math.sin(lat - lat0) * Math.cos(lat + lat0);
			double Mc = ((15 / 8) * n2 + (15 / 8) * n3) * Math.sin(2 * (lat - lat0)) * Math.cos(2 * (lat + lat0));
			double Md = (35 / 24) * n3 * Math.sin(3 * (lat - lat0)) * Math.cos(3 * (lat + lat0));
			M = b * F0 * (Ma - Mb + Mc - Md);                // meridional arc
		} while (N - N0 - M >= 0.00001);  // ie until < 0.01mm

		double cosLat = Math.cos(lat), sinLat = Math.sin(lat);
		double nu = a * F0 / Math.sqrt(1 - e2 * sinLat * sinLat);              // transverse radius of curvature
		double rho = a * F0 * (1 - e2) / Math.pow(1 - e2 * sinLat * sinLat, 1.5);  // meridional radius of curvature
		double eta2 = nu / rho - 1;

		double tanLat = Math.tan(lat);
		double tan2lat = tanLat * tanLat, tan4lat = tan2lat * tan2lat, tan6lat = tan4lat * tan2lat;
		double secLat = 1 / cosLat;
		double nu3 = nu * nu * nu, nu5 = nu3 * nu * nu, nu7 = nu5 * nu * nu;
		double VII = tanLat / (2 * rho * nu);
		double VIII = tanLat / (24 * rho * nu3) * (5 + 3 * tan2lat + eta2 - 9 * tan2lat * eta2);
		double IX = tanLat / (720 * rho * nu5) * (61 + 90 * tan2lat + 45 * tan4lat);
		double X = secLat / nu;
		double XI = secLat / (6 * nu3) * (nu / rho + 2 * tan2lat);
		double XII = secLat / (120 * nu5) * (5 + 28 * tan2lat + 24 * tan4lat);
		double XIIA = secLat / (5040 * nu7) * (61 + 662 * tan2lat + 1320 * tan4lat + 720 * tan6lat);

		double dE = (E - E0),
				dE2 = dE * dE,
				dE3 = dE2 * dE,
				dE4 = dE2 * dE2,
				dE5 = dE3 * dE2,
				dE6 = dE4 * dE2,
				dE7 = dE5 * dE2;
		lat = lat - VII * dE2 + VIII * dE4 - IX * dE6;
		double lon = lon0 + X * dE - XI * dE3 + XII * dE5 - XIIA * dE7;

		return new Location(lat * 180 / Math.PI, lon * 180 / Math.PI);
	}

	// ellipse parameters
	@SuppressWarnings("serial") static Map<String, Ellipse> ellipse = new HashMap<String, Ellipse>() {
		{
			put("WGS84", new Ellipse(6378137, 6356752.3142, 1 / 298.257223563));
			put("GRS80", new Ellipse(6378137, 6356752.314140, 1 / 298.257222101));
			put("Airy1830", new Ellipse(6377563.396, 6356256.910, 1 / 299.3249646));
			put("AiryModified", new Ellipse(6377340.189, 6356034.448, 1 / 299.32496));
			put("Intl1924", new Ellipse(6378388.000, 6356911.946, 1 / 297.0));
		}
	};

	static class Ellipse {

		final double a;
		final double b;
		final double f;

		public Ellipse(double a, double b, double f) {
			this.a = a;
			this.b = b;
			this.f = f;
		}
	}

	// helmert transform parameters from WGS84 to other datums
	@SuppressWarnings("serial") static Map<String, DatumTransform> datumTransform = new HashMap<String, DatumTransform>() {
		{
			put("toOSGB36", new DatumTransform(
					-446.448, 125.157, -542.060, // m
					-0.1502, -0.2470, -0.8421, // sec
					20.4894 // ppm
			));
			put("toED50", new DatumTransform(
					89.5, 93.8, 123.1, // m
					0.0, 0.0, 0.156, // sec
					-1.2 // ppm
			));
			put("toIrl1975", new DatumTransform(
					-482.530, 130.596, -564.557, // m
					-1.042, -0.214, -0.631, // sec
					-8.150 // ppm
			));
		}
	};

	// ED50: og.decc.gov.uk/en/olgs/cms/pons_and_cop/pons/pon4/pon4.aspx
	// strictly, Ireland 1975 is from ETRF89: qv
	// www.osi.ie/OSI/media/OSI/Content/Publications/transformations_booklet.pdf
	// www.ordnancesurvey.co.uk/oswebsite/gps/information/coordinatesystemsinfo/guidecontents/guide6.html#6.5
	static class DatumTransform {
		final double tx;
		final double ty;
		final double tz;
		final double rx;
		final double ry;
		final double rz;
		final double s;

		public DatumTransform(double tx, double ty, double tz, double rx, double ry, double rz, double s) {
			this.tx = tx;
			this.ty = ty;
			this.tz = tz;
			this.rx = rx;
			this.ry = ry;
			this.rz = rz;
			this.s = s;
		}
	}

	/**
	 * Convert lat/lon point in OSGB36 to WGS84
	 *
	 * @param pOSGB36 lat/lon in OSGB36 reference frame
	 * @return lat/lon point in WGS84 reference frame
	 */
	static Location convertOSGB36toWGS84(Location pOSGB36) {
		Ellipse eAiry1830 = ellipse.get("Airy1830");
		Ellipse eWGS84 = ellipse.get("WGS84");
		DatumTransform txToOSGB36 = datumTransform.get("toOSGB36");
		// negate the 'to' transform to get the 'from'
		DatumTransform txFromOSGB36 = new DatumTransform(
				-txToOSGB36.tx, -txToOSGB36.ty, -txToOSGB36.tz,
				-txToOSGB36.rx, -txToOSGB36.ry, -txToOSGB36.rz,
				-txToOSGB36.s
		);
		return convertEllipsoid(pOSGB36, eAiry1830, txFromOSGB36, eWGS84);
	}

	/**
	 * Convert lat/lon from one ellipsoidal model to another
	 *
	 * q.v. Ordnance Survey 'A guide to coordinate systems in Great Britain' Section 6
	 *      www.ordnancesurvey.co.uk/oswebsite/gps/docs/A_Guide_to_Coordinate_Systems_in_Great_Britain.pdf
	 *
	 * @param point lat/lon in source reference frame
	 * @param e1 source ellipse parameters
	 * @param t Helmert transform parameters
	 * @param e2 target ellipse parameters
	 * @return lat/lon in target reference frame
	 */
	private static Location convertEllipsoid(Location point, Ellipse e1, DatumTransform t, Ellipse e2) {
		// console.log('convertEllipsoid', 'geod1', point.toString('dms',4), point.toString('d',6));

		// -- 1: convert polar to cartesian coordinates (using ellipse 1)

		double lat = Math.toRadians(point.getLatitude());
		double lon = Math.toRadians(point.getLongitude());

		double a = e1.a, b = e1.b;
		// console.log('convertEllipsoid', 'a', a, 'b', b);

		double sinPhi = Math.sin(lat);
		double cosPhi = Math.cos(lat);
		double sinLambda = Math.sin(lon);
		double cosLambda = Math.cos(lon);
		double H = 24.7;  // for the moment

		double eSq = (a * a - b * b) / (a * a);
		double nu = a / Math.sqrt(1 - eSq * sinPhi * sinPhi);
		// console.log('convertEllipsoid', 'eSq', eSq, 'nu', nu);

		double x1 = (nu + H) * cosPhi * cosLambda;
		double y1 = (nu + H) * cosPhi * sinLambda;
		double z1 = ((1 - eSq) * nu + H) * sinPhi;
		// console.log('convertEllipsoid', 'cart1', x1, y1, z1);

		// -- 2: apply helmert transform using appropriate params

		double tx = t.tx, ty = t.ty, tz = t.tz;
		double rx = Math.toRadians(t.rx / 3600);  // normalise seconds to radians
		double ry = Math.toRadians(t.ry / 3600);
		double rz = Math.toRadians(t.rz / 3600);
		double s1 = t.s / 1e6 + 1;          // normalise ppm to (s+1)
		// console.log('convertEllipsoid','tx',tx,'ty',ty,'tz',tz,'rx',rx,'ry',ry,'rz',rz,'s',s1);

		// apply transform
		double x2 = tx + x1 * s1 - y1 * rz + z1 * ry;
		double y2 = ty + x1 * rz + y1 * s1 - z1 * rx;
		double z2 = tz - x1 * ry + y1 * rx + z1 * s1;
		// console.log('convertEllipsoid', 'cart2', x2, y1, z2);

		// -- 3: convert cartesian to polar coordinates (using ellipse 2)

		a = e2.a;
		b = e2.b;
		double precision = 4 / a;  // results accurate to around 4 metres
		// console.log('convertEllipsoid', 'a', a, 'b', b);

		eSq = (a * a - b * b) / (a * a);
		double p = Math.sqrt(x2 * x2 + y2 * y2);
		double phi = Math.atan2(z2, p * (1 - eSq)), phiP = 2 * Math.PI;
		while (Math.abs(phi - phiP) > precision) {
			nu = a / Math.sqrt(1 - eSq * Math.sin(phi) * Math.sin(phi));
			phiP = phi;
			phi = Math.atan2(z2 + eSq * nu * Math.sin(phi), p);
		}
		double lambda = Math.atan2(y2, x2);
		H = p / Math.cos(phi) - nu; // TODO what is H?
		//console.log('convertEllipsoid', 'geod2', phi.toDeg(), lambda.toDeg());

		return new Location(Math.toDegrees(phi), Math.toDegrees(lambda));
	}
}
