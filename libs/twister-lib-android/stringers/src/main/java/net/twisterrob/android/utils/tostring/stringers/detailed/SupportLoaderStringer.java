package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.io.*;

import javax.annotation.Nonnull;

import androidx.loader.content.Loader;

import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings({
		"rawtypes", // Cannot register in AndroidStringerRepo if using Loader<?>.
		"deprecation" // Loader.dump
})
public class SupportLoaderStringer extends Stringer<Loader> {
	@Override public void toString(@Nonnull ToStringAppender append, Loader loader) {
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		append.selfDescribingProperty(writer.toString());
	}
}
