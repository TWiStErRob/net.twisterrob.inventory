package net.twisterrob.test.frameworks.classes;

public class Private {
	private String privateMethod() {
		return "real";
	}

	public String publicMethod() {
		return privateMethod();
	}
}
