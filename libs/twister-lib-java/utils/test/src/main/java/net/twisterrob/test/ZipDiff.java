package net.twisterrob.test;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.*;

public class ZipDiff {
	private final ZipIndex before;
	private final ZipIndex after;
	private final Map<ZipEntry, ZipEntry> removed = new HashMap<>();
	private final Map<ZipEntry, ZipEntry> added = new HashMap<>();
	private final Map<ZipEntry, ZipEntry> moved = new HashMap<>();
	private final Map<ZipEntry, ZipEntry> changed = new HashMap<>();
	private final Map<ZipEntry, ZipEntry> same = new HashMap<>();

	public ZipDiff(File before, File after) throws IOException {
		this.before = new ZipIndex(before);
		this.after = new ZipIndex(after);
		detectAddRemoves();
		detectMoves();
		detectSwaps();
		detectReplace();
		detectCollapsed();
		detectCopied();
	}

	public Collection<ZipEntry> getBefore() {
		return Collections.unmodifiableSet(before.set);
	}
	public Collection<ZipEntry> getAfter() {
		return Collections.unmodifiableSet(after.set);
	}
	public Map<ZipEntry, ZipEntry> getRemoved() {
		return Collections.unmodifiableMap(removed);
	}
	public Map<ZipEntry, ZipEntry> getAdded() {
		return Collections.unmodifiableMap(added);
	}
	public Map<ZipEntry, ZipEntry> getMoved() {
		return Collections.unmodifiableMap(moved);
	}
	public Map<ZipEntry, ZipEntry> getChanged() {
		return Collections.unmodifiableMap(changed);
	}
	public Map<ZipEntry, ZipEntry> getSame() {
		return Collections.unmodifiableMap(same);
	}

	private void detectAddRemoves() throws IOException {
		for (ZipEntry beforeEntry : before) {
			Collection<ZipEntry> afterEntries = after.findMatchesByName(beforeEntry);
			if (afterEntries.isEmpty()) {
				safePut(removed, "removed", beforeEntry, null);
			} else {
				for (ZipEntry afterEntry : afterEntries) {
					if (sameContents(before.zip, beforeEntry, after.zip, afterEntry)) {
						safePut(same, "same", beforeEntry, afterEntry);
					} else {
						safePut(changed, "changed", beforeEntry, afterEntry);
					}
				}
			}
		}
		for (ZipEntry afterEntry : after) {
			Collection<ZipEntry> beforeEntries = before.findMatchesByName(afterEntry);
			if (beforeEntries.isEmpty()) {
				safePut(added, "added", afterEntry, null);
			} else {
				// already checked for before
			}
		}
	}
	private void detectMoves() throws IOException {
		Map<ZipEntry, ZipEntry> addAgain = new HashMap<>();
		for (Iterator<ZipEntry> beforeIt = removed.keySet().iterator(); beforeIt.hasNext(); ) {
			ZipEntry beforeEntry = beforeIt.next();
			boolean removeBefore = false;
			for (Iterator<ZipEntry> afterIt = added.keySet().iterator(); afterIt.hasNext(); ) {
				ZipEntry afterEntry = afterIt.next();
				if (sameContents(before.zip, beforeEntry, after.zip, afterEntry)) {
					ZipEntry overridden = moved.put(beforeEntry, afterEntry);
					if (overridden != null) {
						safePut(addAgain, "addAgain", overridden, beforeEntry);
					}
					removeBefore = true;
					afterIt.remove();
				}
			}
			if (removeBefore) {
				beforeIt.remove();
			}
		}
		for (Entry<ZipEntry, ZipEntry> entry : addAgain.entrySet()) {
			safePut(added, "add", entry.getKey(), entry.getValue());
		}
	}
	private void detectReplace() throws IOException {
		Set<ZipEntry> realAdds = new HashSet<>();
		for (Iterator<Entry<ZipEntry, ZipEntry>> afterIt = added.entrySet().iterator(); afterIt.hasNext(); ) {
			Entry<ZipEntry, ZipEntry> afterMap = afterIt.next();
			if (afterMap.getValue() == null) {
				ZipEntry afterEntry = afterMap.getKey();
				for (Iterator<Entry<ZipEntry, ZipEntry>> beforeIt = changed.entrySet().iterator(); beforeIt
						.hasNext(); ) {
					Entry<ZipEntry, ZipEntry> beforeMap = beforeIt.next();
					ZipEntry beforeEntry = beforeMap.getKey();
					if (sameContents(before.zip, beforeEntry, after.zip, afterEntry)) {
						// added: file2(content1), changed: file1(content1)->file1(content2)
						// added: file1(content2), moved: file1(content1)->file2(content1)
						beforeIt.remove();
						afterIt.remove();
						moved.put(beforeEntry, afterEntry);
						realAdds.add(beforeMap.getValue());
					}
				}
			}
		}
		for (ZipEntry afterEntry : realAdds) {
			added.put(afterEntry, null);
		}
	}
	private void detectSwaps() throws IOException {
		Set<ZipEntry> removeBefores = new HashSet<>();
		Set<ZipEntry> removeAfters = new HashSet<>();
		for (ZipEntry beforeEntry : changed.keySet()) {
			for (ZipEntry afterEntry : changed.values()) {
				if (sameContents(before.zip, beforeEntry, after.zip, afterEntry)) {
					safePut(moved, "moved", beforeEntry, afterEntry);
					removeBefores.add(beforeEntry);
					removeAfters.add(afterEntry);
				}
			}
		}
		changed.keySet().removeAll(removeBefores);
		changed.values().removeAll(removeAfters);
	}

	private void detectCollapsed() throws IOException {
		for (Entry<ZipEntry, ZipEntry> beforeEntry : removed.entrySet()) {
			for (ZipEntry afterEntry : after.findMatchesBySize(beforeEntry.getKey())) {
				if (sameContents(before.zip, beforeEntry.getKey(), after.zip, afterEntry)) {
					beforeEntry.setValue(afterEntry);
				}
			}
		}
	}
	private void detectCopied() throws IOException {
		for (Entry<ZipEntry, ZipEntry> afterEntry : added.entrySet()) {
			for (ZipEntry beforeEntry : before.findMatchesBySize(afterEntry.getKey())) {
				if (sameContents(before.zip, beforeEntry, after.zip, afterEntry.getKey())) {
					afterEntry.setValue(beforeEntry);
				}
			}
		}
	}

	private static void safePut(Map<ZipEntry, ZipEntry> map, String type, ZipEntry beforeEntry, ZipEntry afterEntry) {
		ZipEntry overridden = map.put(beforeEntry, afterEntry);
		if (overridden != null) {
			throw new IllegalStateException("Replaced " + type + " mapping for " + beforeEntry + ": "
					+ afterEntry + " overrode: " + overridden);
		}
	}
	private static boolean sameContents(ZipFile zip1, ZipEntry entry1, ZipFile zip2, ZipEntry entry2)
			throws IOException {
		if (entry1.getSize() != entry2.getSize()) {
			return false;
		}
		InputStream stream1 = zip1.getInputStream(entry1);
		InputStream stream2 = zip2.getInputStream(entry2);
		return isEqual(stream1, stream2);
	}

	private static boolean isEqual(InputStream i1, InputStream i2) throws IOException {
		byte[] buf1 = new byte[64 * 1024];
		byte[] buf2 = new byte[64 * 1024];
		try {
			DataInputStream d2 = new DataInputStream(i2);
			int len;
			while ((len = i1.read(buf1)) > 0) {
				d2.readFully(buf2, 0, len);
				for (int i = 0; i < len; i++) {
					if (buf1[i] != buf2[i]) {
						return false;
					}
				}
			}
			return d2.read() < 0; // is the end of the second file also.
		} catch (EOFException ioe) {
			return false;
		} finally {
			try {
				i1.close();
			} catch (IOException ignore) {
			}
			try {
				i2.close();
			} catch (IOException ignore) {
			}
		}
	}

	@Override public String toString() {
		return "ZipDiff{" +
				"removed=" + removed +
				", added=" + added +
				", moved=" + moved +
				", changed=" + changed +
				", same=" + same +
				'}';
	}
	private static class ZipIndex implements Iterable<ZipEntry> {
		private final ZipFile zip;
		private final Set<ZipEntry> set = new HashSet<>();
		private final Map<Long, Set<ZipEntry>> bySize = new HashMap<>();
		private final Map<String, Set<ZipEntry>> byName = new HashMap<>();
		public ZipIndex(File file) throws IOException {
			this.zip = openZip(file);
			if (zip != null) {
				for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
					add(it.nextElement());
				}
			}
		}
		private ZipFile openZip(File file) throws IOException {
			try {
				return new ZipFile(file);
			} catch (ZipException ex) {
				// Work around Android weirdness.
				// java.util.zip.ZipException: Empty zip archive not supported
				//  at java.util.zip.ZipFile.readCentralDir(ZipFile.java:382)
				if (!"Empty zip archive not supported".equals(ex.getMessage())) {
					throw ex;
				}
				return null;
			}
		}

		@Override public Iterator<ZipEntry> iterator() {
			return set.iterator();
		}
		private void add(ZipEntry entry) {
			set.add(entry);
			addBySize(entry);
			addByName(entry);
		}
		private void addBySize(ZipEntry entry) {
			Set<ZipEntry> group = bySize.get(entry.getSize());
			if (group == null) {
				group = new HashSet<>();
				bySize.put(entry.getSize(), group);
			}
			group.add(entry);
		}
		private void addByName(ZipEntry entry) {
			Set<ZipEntry> group = byName.get(entry.getName());
			if (group == null) {
				group = new HashSet<>();
				byName.put(entry.getName(), group);
			}
			group.add(entry);
		}

		public Collection<ZipEntry> findMatchesByName(ZipEntry entry) throws IOException {
			Set<ZipEntry> entries = byName.get(entry.getName());
			return entries != null? entries : Collections.<ZipEntry>emptySet();
		}
		public Collection<ZipEntry> findMatchesBySize(ZipEntry entry) throws IOException {
			Set<ZipEntry> entries = bySize.get(entry.getSize());
			return entries != null? entries : Collections.<ZipEntry>emptySet();
		}
	}
}
