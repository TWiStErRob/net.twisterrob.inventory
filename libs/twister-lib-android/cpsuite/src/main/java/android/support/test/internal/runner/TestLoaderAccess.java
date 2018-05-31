package android.support.test.internal.runner;

public class TestLoaderAccess {
	public Class<?> loadClass(String name) {
		try {
			return Class.forName(name, false, TestLoader.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
