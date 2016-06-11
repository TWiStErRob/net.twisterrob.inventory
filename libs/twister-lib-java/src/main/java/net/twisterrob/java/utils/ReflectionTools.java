package net.twisterrob.java.utils;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import org.slf4j.*;

public class ReflectionTools {
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionTools.class);

	@SuppressWarnings("unchecked")
	public static <T> T getStatic(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
		try {
			Field field = findDeclaredField(clazz, fieldName);
			field.setAccessible(true);
			return (T)field.get(null);
		} catch (Exception ex) {
			LOG.warn("Cannot read static field {} of {}", fieldName, clazz, ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStatic(@Nonnull String className, @Nonnull String fieldName) {
		try {
			return getStatic(Class.forName(className), fieldName);
		} catch (Exception ex) {
			LOG.warn("Cannot read static field {} of {}", fieldName, className, ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(@Nonnull Object object, @Nonnull String fieldName) {
		try {
			Field field = findDeclaredField(object.getClass(), fieldName);
			field.setAccessible(true);
			return (T)field.get(object);
		} catch (Exception ex) {
			//noinspection ConstantConditions prevent NPE when object is null, even though it was declared not null
			LOG.warn("Cannot read field {} of ({}){}", fieldName, object != null? object.getClass() : null, object, ex);
		}
		return null;
	}

	/**
	 * Like {@link Class#getDeclaredField}, but looking in all superclasses as well.
	 * @throws NoSuchFieldException if a field with the specified name is not found in the class hierarchy.
	 * @see Class#getDeclaredField(String)
	 */
	public static Field findDeclaredField(@Nonnull Class<?> clazz, @Nonnull String fieldName)
			throws NoSuchFieldException {
		do {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ex) {
				clazz = clazz.getSuperclass();
			}
		} while (clazz != null);
		throw new NoSuchFieldException(fieldName);
	}

	public static boolean instanceOf(String clazz, Object value) {
		try {
			return Class.forName(clazz).isInstance(value);
		} catch (ClassNotFoundException ex) {
			LOG.warn("Cannot find class {} to check instanceof {}", clazz, value, ex);
			return false;
		}
	}
}
