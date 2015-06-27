package net.twisterrob.inventory.database;

class LevelBasedID {
	static final int MAX_PER_LEVEL = 10;
	static final int MAX_LEVEL = 4;
	private final int[] levels = new int[MAX_LEVEL];
	int lastLevel = 0;

	private int composeID() {
		int id = 0;
		for (int level = 0; level < levels.length; level++) {
			id += Math.pow(10, MAX_LEVEL - level - 1) * levels[level];
		}
		return id;
	}

	public int newItem(int level) {
		if (level < 0 || MAX_LEVEL <= level) {
			throw new IndexOutOfBoundsException(
					"Invalid level: " + level + ", must be between 0 and " + (MAX_LEVEL - 1));
		}
		if (0 < level && levels[level] == MAX_PER_LEVEL - 1) {
			throw new IllegalStateException(
					"Level " + level + " cannot have more than " + MAX_PER_LEVEL + " items.");
		}
		if (lastLevel + 1 < level) {
			throw new IllegalStateException("Cannot go deeper with skipping intermediate levels."
					+ " Last: " + lastLevel + " current: " + level);
		}
		if (level < lastLevel) {
			for (int lvl = lastLevel; level < lvl; --lvl) {
				levels[lvl] = 0;
			}
		}
		lastLevel = level;
		levels[level]++;
		return composeID();
	}
}
