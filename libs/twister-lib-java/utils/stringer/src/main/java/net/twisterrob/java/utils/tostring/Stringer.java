package net.twisterrob.java.utils.tostring;

import javax.annotation.Nonnull;

import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public abstract class Stringer<T> {
	public abstract void toString(@Nonnull ToStringAppender append, T object);

	public String getType(T object) {
		return DefaultStringer.debugType(object);
	}
}
