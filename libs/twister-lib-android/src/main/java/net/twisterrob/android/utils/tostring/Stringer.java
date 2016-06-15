package net.twisterrob.android.utils.tostring;

import android.support.annotation.NonNull;

public interface Stringer<T> {
	public abstract @NonNull String toString(T object);

//	public String getType(T object) {
//		return AndroidTools.debugType(object);
//	}
}
