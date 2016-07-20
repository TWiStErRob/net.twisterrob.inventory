package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.location.Address;

import net.twisterrob.java.utils.tostring.*;

/**
 * Original toString: <pre>Address[addressLines=[0:"29 Brackley Road",1:"Beckenham",2:"BR3 1RX",3:"UK"],feature=29,admin=null,sub-admin=null,locality=Beckenham,thoroughfare=Brackley Road,postalCode=BR3 1RX,countryCode=GB,countryName=United Kingdom,hasLatitude=true,latitude=51.4163147,hasLongitude=true,longitude=-0.0318984,phone=null,url=null,extras=null]</pre>
 */
public class AddressStringer extends Stringer<Address> {
	@Override public void toString(@Nonnull ToStringAppender append, Address address) {
		append.beginPropertyGroup("Local");
		append.rawProperty("featureName", address.getFeatureName());
		append.rawProperty("premises", address.getPremises());
		append.rawProperty("locality", address.getLocality());
		append.rawProperty("subLocality", address.getSubLocality());
		append.rawProperty("thoroughfare", address.getThoroughfare());
		append.rawProperty("subThoroughfare", address.getSubThoroughfare());
		append.endPropertyGroup();

		append.beginSizedList("addressLine", address.getMaxAddressLineIndex(), true);
		for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
			append.item(i, address.getAddressLine(i));
		}
		append.endSizedList();

		append.beginPropertyGroup("Global");
		append.rawProperty("postalCode", address.getPostalCode());
		append.rawProperty("subAdmin", address.getSubAdminArea());
		append.rawProperty("admin", address.getAdminArea());
		append.rawProperty("countryCode", address.getCountryCode());
		append.rawProperty("country", address.getCountryName());
		append.endPropertyGroup();

		append.beginPropertyGroup("Other");
		append.rawProperty("phone", address.getPhone());
		append.rawProperty("url", address.getUrl());
		if (address.hasLatitude()) {
			append.rawProperty("latitude", address.getLatitude());
		}
		if (address.hasLongitude()) {
			append.rawProperty("longitude", address.getLongitude());
		}
		append.complexProperty("extras", address.getExtras());
		append.endPropertyGroup();
	}
}
