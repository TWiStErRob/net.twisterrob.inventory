package net.twisterrob.inventory.android.backup;

import java.io.IOException;
import java.util.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

import com.shazam.gwen.collaborators.Asserter;

import net.twisterrob.inventory.android.content.Database;

class BackupImageDatabase implements Asserter {
	private final Database db;
	private long imageId = 100;
	private Map<String, Long> imageIds = new HashMap<>();
	private Map<Long, byte[]> imageContents = new HashMap<>();
	private Map<Long, Long> itemImages = new HashMap<>();
	public BackupImageDatabase(Database mock) {
		db = mock;
		Answer<Long> insertImage = new Answer<Long>() {
			@Override public Long answer(InvocationOnMock invocation) throws Throwable {
				byte[] contents = invocation.getArgumentAt(0, byte[].class);
				assertThat(imageContents, not(hasValue(contents)));
				imageContents.put(imageId, contents);
				imageIds.put(BackupZip.getName(contents), imageId);
				return imageId++; // generate a unique ID for this run
			}
		};
		Answer<Void> deleteImage = new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				long imageId = invocation.getArgumentAt(0, Long.TYPE);
				assertThat(imageContents, hasKey(imageId));
				imageContents.remove(imageId);
				return null;
			}
		};
		Answer<Void> associateImage = new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				long id = invocation.getArgumentAt(0, Long.TYPE);
				long imageId = invocation.getArgumentAt(1, Long.TYPE);
				assertThat(itemImages, not(hasKey(id)));
				assertThat(itemImages, not(hasValue(imageId)));
				itemImages.put(id, imageId);
				return null;
			}
		};
		doAnswer(insertImage).when(db).addImage(any(byte[].class), any(Long.class));
		doAnswer(associateImage).when(db).setItemImage(anyLong(), any(Long.class));
		doAnswer(deleteImage).when(db).deleteImage(any(Long.class));
	}

	/** Images that are added and used for a belonging */
	public BackupImageDatabase matchedImages(String... images) throws IOException {
		for (String image : images) {
			verify(db).addImage(aryEq(BackupZip.getContents(image)), any(Long.class));
			verify(db).setItemImage(anyLong(), eq(imageIds.get(image)));
		}
		return this;
	}

	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're after the data file, they should never be added, at that point we know if they're needed or not. */
	public BackupImageDatabase danglingImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, never()).addImage(aryEq(BackupZip.getContents(image)), any(Long.class));
			//verify(db, never()).deleteImage(eq(imageIds.get(image))); // can't verify, if it wasn't added, there's no id
			verify(db, never()).setItemImage(anyLong(), eq(imageIds.get(image)));
		}
		return this;
	}

	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're before the data file, they may be added an then deleted, because it's not know if they're needed. */
	public BackupImageDatabase redundantImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, atMost(1)).addImage(aryEq(BackupZip.getContents(image)), any(Long.class));
			verify(db, atMost(1)).deleteImage(eq(imageIds.get(image)));
		}
		return this;
	}
}
