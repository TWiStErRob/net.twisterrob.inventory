package net.twisterrob.android.utils.tostring.stringers.name;

import javax.annotation.Nonnull;

import android.location.Address;

import net.twisterrob.java.utils.StringTools;
import net.twisterrob.java.utils.tostring.*;

/**
 * Original toString: <pre>Address[addressLines=[0:"29 Brackley Road",1:"Beckenham",2:"BR3 1RX",3:"UK"],feature=29,admin=null,sub-admin=null,locality=Beckenham,thoroughfare=Brackley Road,postalCode=BR3 1RX,countryCode=GB,countryName=United Kingdom,hasLatitude=true,latitude=51.4163147,hasLongitude=true,longitude=-0.0318984,phone=null,url=null,extras=null]</pre>
 */
public class AddressNameStringer extends Stringer<Address> {
	@Override public void toString(@Nonnull ToStringAppender append, Address address) {
		if (address == null) {
			append.selfDescribingProperty(StringTools.NULL_STRING);
			return;
		}
		append.identity(StringTools.hashString(address),
				address.getFeatureName() + ", " + address.getPostalCode() + " " + address.getCountryCode());
	}
}
