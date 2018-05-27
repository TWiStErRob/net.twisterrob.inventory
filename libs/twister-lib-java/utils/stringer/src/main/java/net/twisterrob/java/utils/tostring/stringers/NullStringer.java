package net.twisterrob.java.utils.tostring.stringers;

import javax.annotation.Nonnull;

import net.twisterrob.java.utils.tostring.*;

public final class NullStringer extends Stringer<Object> {
	public static final Stringer<Object> INSTANCE = new NullStringer();

	private NullStringer() {
		// prevent instantiation
	}

	@Override public String getType(Object object) {
		return null;
	}
	@Override public void toString(@Nonnull ToStringAppender append, Object object) {
		append.identity("<null>", null);
	}
}
