package net.twisterrob.test;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.test.hamcrest.IsMapContainsEntries;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class ZipDiffTest {
	private static final Logger LOG = LoggerFactory.getLogger(ZipDiffTest.class);

	private static final ZipEntry[] EMPTY = new ZipEntry[0];

	@Rule public final TemporaryFolder temp = new TemporaryFolder();

	@Test public void testEmpty() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.build();
		File afterFile = new Zip(temp.newFile())
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testAdded() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), nullValue())
		));
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testRemoved() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), nullValue())
		));
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testChanged() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents2")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file1"))
		));
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testSame() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file1"))
		));
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testMove() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file2", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file2"))
		));
	}

	@Test public void testReplaced() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents2")
				.withFile("file2", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), nullValue())
		));
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file2"))
		));
	}

	@Test public void testSwapped() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents2")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents2")
				.withFile("file2", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file2")),
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file1"))
		));
	}

	@Test public void testShuffle() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents2")
				.withFile("file3", "contents3")
				.withFile("file4", "contents4")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents2")
				.withFile("file2", "contents4")
				.withFile("file3", "contents1")
				.withFile("file4", "contents3")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file3")),
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file1")),
				hasEntry(zipEntryWithName("file3"), zipEntryWithName("file4")),
				hasEntry(zipEntryWithName("file4"), zipEntryWithName("file2"))
		));
	}

	@Test public void testMoveDuplicate() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file2", "contents1")
				.withFile("file3", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), exactlyOneOf(
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file1")),
				hasEntry(zipEntryWithName("file3"), zipEntryWithName("file1"))
		));
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), exactlyOneOf(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file2")),
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file3"))
		));
		// make the two exactlyOneOf matches exclusive
		assertThat(diff.getMoved(), not(hasValue(diff.getAdded().keySet().iterator().next())));
	}

	@Test public void testCopied() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file1"))
		));
		assertThat(diff.getRemoved(), anEmptyMap());
		assertThat(diff.getSame(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file1"))
		));
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testCollapsed() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file3", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), exactlyOneOf(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file3")),
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file3"))
		));
		assertThat(diff.getSame(), anEmptyMap());
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), exactlyOneOf(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file3")),
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file3"))
		));
		// make the two exactlyOneOf matches exclusive
		assertThat(diff.getMoved(), not(hasKey(diff.getRemoved().keySet().iterator().next())));
	}

	@Test public void testCollapsedIntoFirst() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file1"))
		));
		assertThat(diff.getSame(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file1"))
		));
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testCollapsedIntoSecond() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("file1", "contents1")
				.withFile("file2", "contents1")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("file2", "contents1")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), anEmptyMap());
		assertThat(diff.getRemoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file1"), zipEntryWithName("file2"))
		));
		assertThat(diff.getSame(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("file2"), zipEntryWithName("file2"))
		));
		assertThat(diff.getChanged(), anEmptyMap());
		assertThat(diff.getMoved(), anEmptyMap());
	}

	@Test public void testAll() throws IOException {
		File beforeFile = new Zip(temp.newFile())
				.withFile("removed1", "removed1")
				.withFile("removed2", "removed2")
				.withFile("changed1", "changed")
				.withFile("changed2", "changed")
				.withFile("replaced", "replaced")
				.withFile("swapped1", "swapped1")
				.withFile("swapped2", "swapped2")
				.withFile("same1", "same1")
				.withFile("same2", "same2")
				.withFile("copied", "copied")
				.withFile("merged1", "merged")
				.withFile("merged2", "merged")
				.withFile("merged", "merged")
				.build();
		File afterFile = new Zip(temp.newFile())
				.withFile("swapped2", "swapped1")
				.withFile("copied", "copied")
				.withFile("same1", "same1")
				.withFile("changed1", "changed contents")
				.withFile("copied1", "copied")
				.withFile("added1", "added1")
				.withFile("replaced", "replacement")
				.withFile("moved", "replaced")
				.withFile("changed2", "changed contents")
				.withFile("added2", "added2")
				.withFile("swapped1", "swapped2")
				.withFile("copied2", "copied")
				.withFile("same2", "same2")
				.withFile("merged", "merged")
				.build();

		ZipDiff diff = new ZipDiff(beforeFile, afterFile);

		assertInvariants(diff);
		assertThat(diff.getAdded(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("added1"), nullValue()),
				hasEntry(zipEntryWithName("added2"), nullValue()),
				hasEntry(zipEntryWithName("replaced"), nullValue()),
				hasEntry(zipEntryWithName("copied1"), zipEntryWithName("copied")),
				hasEntry(zipEntryWithName("copied2"), zipEntryWithName("copied"))
		));
		assertThat(diff.getRemoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("removed1"), nullValue()),
				hasEntry(zipEntryWithName("removed2"), nullValue()),
				hasEntry(zipEntryWithName("merged1"), zipEntryWithName("merged")),
				hasEntry(zipEntryWithName("merged2"), zipEntryWithName("merged"))
		));
		assertThat(diff.getSame(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("same1"), zipEntryWithName("same1")),
				hasEntry(zipEntryWithName("same2"), zipEntryWithName("same2")),
				hasEntry(zipEntryWithName("merged"), zipEntryWithName("merged")),
				hasEntry(zipEntryWithName("copied"), zipEntryWithName("copied"))
		));
		assertThat(diff.getChanged(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("changed1"), zipEntryWithName("changed1")),
				hasEntry(zipEntryWithName("changed2"), zipEntryWithName("changed2"))
		));
		assertThat(diff.getMoved(), IsMapContainsEntries.containsEntries(
				hasEntry(zipEntryWithName("swapped1"), zipEntryWithName("swapped2")),
				hasEntry(zipEntryWithName("swapped2"), zipEntryWithName("swapped1")),
				hasEntry(zipEntryWithName("replaced"), zipEntryWithName("moved"))
		));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void assertInvariants(ZipDiff d) {
		LOG.trace("{}", d);
		// all the following is coming from before
		assertThat(d.getBefore(), hasItems(d.getRemoved().keySet().toArray(EMPTY)));
		assertThat(d.getBefore(), hasItems(d.getSame().keySet().toArray(EMPTY)));
		assertThat(d.getBefore(), hasItems(d.getChanged().keySet().toArray(EMPTY)));
		assertThat(d.getBefore(), hasItems(d.getMoved().keySet().toArray(EMPTY)));
		// all the following is coming from after
		assertThat(d.getAfter(), hasItems(d.getAdded().keySet().toArray(EMPTY)));
		assertThat(d.getAfter(), hasItems(d.getSame().values().toArray(EMPTY)));
		assertThat(d.getAfter(), hasItems(d.getChanged().values().toArray(EMPTY)));
		assertThat(d.getAfter(), hasItems(d.getMoved().values().toArray(EMPTY)));
		// there are no extras in before/after (these splits cover the whole input)
		assertThat(d.getBefore(), containsInAnyOrder((List)collect(
				d.getRemoved().keySet(), d.getSame().keySet(), d.getChanged().keySet(), d.getMoved().keySet())));
		assertThat(d.getAfter(), containsInAnyOrder((List)collect(
				d.getAdded().keySet(), d.getSame().values(), d.getChanged().values(), d.getMoved().values())));
	}

	@SafeVarargs
	private static List<Matcher<ZipEntry>> collect(Collection<ZipEntry>... colls) {
		List<Matcher<ZipEntry>> all = new ArrayList<>();
		for (Collection<ZipEntry> coll : colls) {
			for (ZipEntry entry : coll) {
				all.add(sameInstance(entry));
			}
		}
		return all;
	}

	private static class Zip {
		public static final byte[] EMPTY_ZIP = {
				0x50, 0x4b, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		};
		private final File target;
		private final ZipOutputStream zipStream;
		public Zip(File target) throws FileNotFoundException {
			this.target = target;
			zipStream = new ZipOutputStream(new FileOutputStream(target));
		}
		public Zip withFile(String name, byte... contents) throws IOException {
			IOTools.zip(zipStream, name, new ByteArrayInputStream(contents));
			return this;
		}
		public Zip withFile(String name, String contents) throws IOException {
			return withFile(name, contents.getBytes("UTF-8"));
		}
		public File build() throws IOException {
			try {
				zipStream.close();
			} catch (ZipException ex) {
				if ("No entries".equals(ex.getMessage())) {
					withFile("dummy", "so I can close the zip");
					zipStream.close();
					IOTools.writeAll(new FileOutputStream(target), EMPTY_ZIP);
				} else {
					throw ex;
				}
			}
			return target;
		}
	}
}
