package net.twisterrob.java.utils.tostring.stringers;

import javax.annotation.Nonnull;

import net.twisterrob.java.utils.StringTools;
import net.twisterrob.java.utils.tostring.*;

public class DefaultNameStringer extends Stringer<Object> {
	public static final Stringer<Object> INSTANCE = new DefaultNameStringer();

	@Override public void toString(@Nonnull ToStringAppender append, Object object) {
		if (object == null) {
			append.selfDescribingProperty("<null>");
			return;
		}
		Class<?> clazz = object.getClass();
		String className = clazz.getSimpleName();
		if (StringTools.isNullOrEmpty(className)) {
			className = clazz.getName();
			if (className != null) {
				className = className.substring(className.lastIndexOf('.'));
				if (className.endsWith(";")) {
					// unknown dimensioned array
					className = className.substring(0, className.length() - 1) + "[?]";
				}
			}
		}
		// TODO use className?
		append.selfDescribingProperty("@" + StringTools.hashString(object));
	}
}
