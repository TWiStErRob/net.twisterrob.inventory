package net.twisterrob.java.model;
public class LocationConverter {
	public static net.twisterrob.java.model.Location gridRef2LatLon(int easting, int northing) {
		Location OSGB36 = MoveableTypeGridRefLocationConverter.f(easting, northing);
		Location WGS84 = MoveableTypeGridRefLocationConverter.convertOSGB36toWGS84(OSGB36);
		return WGS84;
	}
}
