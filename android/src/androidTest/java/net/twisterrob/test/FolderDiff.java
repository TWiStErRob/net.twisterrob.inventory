package net.twisterrob.test;

import java.io.File;
import java.util.*;

import static org.hamcrest.io.FileMatchers.*;
import static org.junit.Assert.*;

public class FolderDiff {
	private final File[] contents;
	private final File folder;
	public FolderDiff(File folder) {
		assertThat(folder, anExistingDirectory());
		this.folder = folder;
		this.contents = folder.listFiles();
	}
	public Collection<File> getBefore() {
		return Arrays.asList(contents);
	}
	public Collection<File> getAfter() {
		return Arrays.asList(folder.listFiles());
	}
	public Collection<File> getAdded() {
		Set<File> files = new HashSet<>(getAfter());
		files.removeAll(getBefore());
		return files;
	}
	public Collection<File> getRemoved() {
		Set<File> files = new HashSet<>(getBefore());
		files.removeAll(getAfter());
		return files;
	}
}
