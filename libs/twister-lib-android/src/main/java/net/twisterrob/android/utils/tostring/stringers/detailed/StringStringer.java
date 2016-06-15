package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tostring.Stringer;

public class StringStringer implements Stringer<String> {
	@Override public @NonNull String toString(String object) {
		return '"' + object + '"';
	}
}
