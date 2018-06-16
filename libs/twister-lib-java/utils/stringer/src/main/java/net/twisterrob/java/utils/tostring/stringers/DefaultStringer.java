package net.twisterrob.java.utils.tostring.stringers;

import java.lang.reflect.Array;
import java.util.*;

import javax.annotation.*;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.StringTools;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class DefaultStringer extends Stringer<Object> {
	public static final Stringer<Object> INSTANCE = new DefaultStringer();
	private static final Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<>();

	static {
		PRIMITIVES.put(Boolean.class, Boolean.TYPE);
		PRIMITIVES.put(Byte.class, Byte.TYPE);
		PRIMITIVES.put(Short.class, Short.TYPE);
		PRIMITIVES.put(Integer.class, Integer.TYPE);
		PRIMITIVES.put(Long.class, Long.TYPE);
		PRIMITIVES.put(Float.class, Float.TYPE);
		PRIMITIVES.put(Double.class, Double.TYPE);
		PRIMITIVES.put(Character.class, Character.TYPE);
	}

	@Override public void toString(@Nonnull ToStringAppender append, @Nullable Object value) {
		if (value == null) {
			append.selfDescribingProperty(StringTools.NULL_STRING);
			return;
		}
		String display = getDisplay(value);
		display = fixType(value, display);
		append.selfDescribingProperty(display);
	}

	protected @Nonnull String getDisplay(@Nonnull Object value) {
		String display;
		if (value.getClass().isArray()) {
			if (value.getClass().getComponentType().isPrimitive()) {
				display = Arrays.deepToString(new Object[] {value});
				display = display.substring(1, display.length() - 1);
			} else {
				display = Arrays.deepToString((Object[])value);
			}
		} else {
			display = value.toString();
		}
		return shortenPackageNames(display);
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	protected String fixType(@Nullable Object value, @Nonnull String display) {
		String type = getType(value);
		if (type == null) {
			return display;
		}
		int typeLen = type.length();
		int displayLen = display.length();
		if (typeLen <= displayLen && display.startsWith(type)) {
			if (typeLen == displayLen) {
				display = "";
			} else {
				int posAfterType = typeLen;
				char first = display.charAt(posAfterType);
				if (first == '@') { // "Object@xxxxxxxx"
					display = display.substring(posAfterType);
				} else if (first == '{') { // { "android View{properties}"
					display = display.substring(posAfterType);
				} else if (first == '[') { // { "swing Component[properties]"
					display = display.substring(posAfterType);
				} else if (first == ':' && value instanceof Throwable) { // "Exception: message"
					if (typeLen + 2 <= displayLen && display.charAt(posAfterType + 1) == ' ') {
						display = display.substring(posAfterType + 2); // skip both : and space
					} else {
						display = display.substring(posAfterType); // : not followed by space, don't strip anything
					}
				}
			}
		}
		return display;
	}

	public static @Nonnull String debugType(@Nullable Object value) {
		if (value == null) {
			return StringTools.NULL_STRING;
		}
		Class<?> clazz = value.getClass();
		return debugTypeName(clazz, value);
	}

	private static @Nonnull String debugTypeName(@Nonnull Class<?> clazz, @Nullable Object value) {
		Class<?> primitive = PRIMITIVES.get(clazz);
		if (primitive != null) {
			clazz = primitive;
		}
		String name = clazz.getCanonicalName();
		if (clazz.isArray() && value != null) {
			// CONSIDER multidimensional cases String[5][6][][]
			name = name.replaceFirst("\\[]", "[" + Array.getLength(value) + "]");
		}
		if (name == null) {
			name = clazz.toString();
		}
		return shortenPackageNames(name);
	}

	public static @Nonnull String shortenPackageNames(@Nonnull String string) {
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+(v4|v7|v13)\\.(?:[a-z0-9]+\\.)+", "$1.");
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^java\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^javax\\.swing\\.(?:[a-z0-9]+\\.)*", "swing.");
		string = string.replaceAll("^javax\\.(?:[a-z0-9]+\\.)+", "jx.");
		string = string.replaceAll("^com.google.android.gms\\.(?:[a-z0-9]+\\.)+", "gms.");
		string = string.replaceAll("^net\\.twisterrob\\.([a-z0-9.]+\\.)*", "tws.");
		return string;
	}
}
