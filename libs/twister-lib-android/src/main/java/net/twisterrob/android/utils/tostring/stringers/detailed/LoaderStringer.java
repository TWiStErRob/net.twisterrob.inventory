package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.io.*;

import android.annotation.TargetApi;
import android.content.Loader;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tostring.Stringer;

@TargetApi(VERSION_CODES.HONEYCOMB)
public class LoaderStringer implements Stringer<Loader<?>> {
	@Override public @NonNull String toString(Loader<?> loader) {
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		return writer.toString();
	}
}
