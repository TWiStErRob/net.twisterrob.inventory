package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.io.*;

import javax.annotation.Nonnull;

import android.os.Build.VERSION_CODES;

import androidx.annotation.RequiresApi;

import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings({"rawtypes", "deprecation"})
@RequiresApi(VERSION_CODES.HONEYCOMB)
public class LoaderStringer extends Stringer<android.content.Loader> {
	@Override public void toString(@Nonnull ToStringAppender append, android.content.Loader loader) {
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		append.selfDescribingProperty(writer.toString());
	}
}
