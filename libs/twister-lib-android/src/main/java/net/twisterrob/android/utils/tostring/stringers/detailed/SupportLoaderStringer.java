package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.io.*;

import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import net.twisterrob.android.utils.tostring.Stringer;

public class SupportLoaderStringer implements Stringer<Loader<?>> {
	@Override public @NonNull String toString(Loader<?> loader) {
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		return writer.toString();
	}
}
