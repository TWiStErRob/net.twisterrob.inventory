package net.twisterrob.test;

import java.io.*;

import org.mockito.ArgumentMatcher;

public class SameFile implements ArgumentMatcher<File> {
	private final File file;

	public SameFile(File file) {
		this.file = file;
	}

	@Override public boolean matches(File argument) {
		if (argument == null) {
			return file == null;
		} else if (file == null) {
			return false;
		}
		try {
			File actual = argument.getCanonicalFile();
			File expected = file.getCanonicalFile();
			return expected.equals(actual);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override public String toString() {
		return file != null? file.toString() : "null File";
	}

	public static ArgumentMatcher<File> pointsTo(final File file) {
		return new SameFile(file);
	}
}
