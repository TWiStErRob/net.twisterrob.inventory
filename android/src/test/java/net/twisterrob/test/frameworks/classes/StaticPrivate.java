package net.twisterrob.test.frameworks.classes;

public class StaticPrivate {

	private static String privateStaticMethod() {
		return "real";
	}

	public static String publicStaticMethod() {
		return privateStaticMethod();
	}
}
