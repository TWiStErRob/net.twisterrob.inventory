package net.twisterrob.java.model;

class LocationToScreenTransformer {
	private double minLat;
	private double maxLat;
	private double scaleY;
	private int alignY;

	private double minLon;
	private double maxLon;
	private double scaleX;
	private int alignX;

	public LocationToScreenTransformer(Iterable<Location> universe) {
		reset();
		for (Location loc: universe) {
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			if (lat < minLat) {
				minLat = lat;
			}
			if (lat > maxLat) {
				maxLat = lat;
			}
			if (lon < minLon) {
				minLon = lon;
			}
			if (lon > maxLon) {
				maxLon = lon;
			}
		}
	}

	public void init(int width, int height) {
		resetTranform();
		scaleX = width / (maxLon - minLon);
		scaleY = height / (maxLat - minLat);
		scaleX = Math.min(scaleX, scaleY); // preserve aspect ratio
		scaleY = /* reverse vertically */-1 * scaleX; // preserve aspect ratio
		alignX = (width - lon2Screen(maxLon)) / 2; // center horizontally
		alignY = (height - lat2Screen(maxLat)) / 2; // center vertically
	}

	protected void reset() {
		minLat = Double.POSITIVE_INFINITY;
		maxLat = Double.NEGATIVE_INFINITY;
		minLon = Double.POSITIVE_INFINITY;
		maxLon = Double.NEGATIVE_INFINITY;
		resetTranform();
	}

	protected void resetTranform() {
		scaleX = scaleY = 1;
		alignX = alignY = 0;
	}

	public int lon2Screen(double longitude) {
		return alignX + (int)((longitude - minLon) * scaleX);
	}
	public int lat2Screen(double latitude) {
		return alignY + (int)((latitude - minLat) * scaleY);
	}
	public double screen2Lon(int x) {
		return (x - alignX) / scaleX + minLon;
	}
	public double screen2Lat(int y) {
		return (y - alignY) / scaleY + minLat;
	}
}