package net.twisterrob.java.utils;

import java.lang.reflect.*;

import org.junit.Test;

public class ArrayToolsTest {
	@Test(expected = InvocationTargetException.class)
	public void testStaticClass() throws ReflectiveOperationException {
		Constructor<ArrayTools> constructor = ArrayTools.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
