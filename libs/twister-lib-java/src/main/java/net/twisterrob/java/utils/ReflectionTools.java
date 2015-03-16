package net.twisterrob.java.utils;

import java.lang.reflect.Field;

import org.slf4j.*;

public class ReflectionTools {
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionTools.class);

	@SuppressWarnings("unchecked")
	public static <T> T get(Object object, String fieldName) {
		try {
			Field field = findDeclaredField(object.getClass(), fieldName);
			field.setAccessible(true);
			return (T)field.get(object);
		} catch (Exception ex) {
			LOG.warn("Cannot get LoaderManager.mWho", ex);
		}
		return null;
	}

	public static Field findDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		do {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ex) {
				clazz = clazz.getSuperclass();
			}
		} while (clazz != null);
		throw new NoSuchFieldException(fieldName);
	}
}
