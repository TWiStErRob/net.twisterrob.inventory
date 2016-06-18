package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.io.*;

import javax.annotation.Nonnull;

import android.annotation.TargetApi;
import android.content.Loader;
import android.os.Build.VERSION_CODES;

import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings("rawtypes")
@TargetApi(VERSION_CODES.HONEYCOMB)
public class LoaderStringer extends Stringer<Loader> {
	@Override public void toString(@Nonnull ToStringAppender append, Loader loader) {
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		append.selfDescribingProperty(writer.toString());
	}
}
