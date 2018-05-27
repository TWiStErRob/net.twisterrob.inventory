package net.twisterrob.java.utils.tostring.stringers;

import javax.annotation.Nonnull;

import net.twisterrob.java.utils.tostring.*;

public class StringStringer extends Stringer<String> {
	@Override public String getType(String object) {
		return null;
	}
	@Override public void toString(@Nonnull ToStringAppender append, String object) {
		append.selfDescribingProperty('"' + object + '"');
	}
}
