package net.twisterrob.java.utils.tostring;

public class IndentLevel {
	private int level;
	public IndentLevel() {
		reset();
	}
	public IndentLevel(int level) {
		setLevel(level);
	}
	public int getLevel() {
		return level;
	}
	public void indent() {
		setLevel(level + 1);
	}
	public void unindent() {
		setLevel(level - 1);
	}
	public void reset() {
		setLevel(0);
	}
	public void setLevel(int newLevel) {
		assertValidIndent(newLevel);
		this.level = newLevel;
	}
	private static void assertValidIndent(int level) {
		if (level < 0) {
			throw new IllegalStateException("Cannot use negative indent value.");
		}
	}
}
