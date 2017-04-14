package net.twisterrob.test.frameworks.classes;

public class Recipient {
	private final Mockable mockable;

	public Recipient(Mockable mockable) {
		this.mockable = mockable;
	}

	public Mockable getMockable() {
		return mockable;
	}
}
