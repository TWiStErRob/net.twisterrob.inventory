package net.twisterrob.inventory.android.backup;

import java.io.IOException;
import java.util.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

import com.shazam.gwen.collaborators.Asserter;

import net.twisterrob.inventory.android.content.Database;

class BackupImageDatabase implements Asserter {
	private final Database db;
	private long imageId = 100;
	// FIXME reverse engineer contents to associate with filenames and remove anyLong()s below
	private Map<Long, byte[]> imageContents = new HashMap<>();
	private Map<Long, Long> itemImages = new HashMap<>();
	public BackupImageDatabase(Database mock) {
		db = mock;
		Answer<Long> insertImage = new Answer<Long>() {
			@Override public Long answer(InvocationOnMock invocation) throws Throwable {
				byte[] contents = invocation.getArgumentAt(0, byte[].class);
				assertThat(imageContents, not(hasValue(contents)));
				imageContents.put(imageId, contents);
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
		doAnswer(insertImage).when(db).addImage(any(byte[].class), anyLong());
		doAnswer(associateImage).when(db).setItemImage(anyLong(), anyLong());
		doAnswer(deleteImage).when(db).deleteImage(anyLong());
	}
	public BackupImageDatabase transacted() {
		verify(db).beginTransaction();
		verify(db).endTransaction();
		return this;
	}
	public BackupImageDatabase successfully() {
		verify(db).setTransactionSuccessful();
		return this;
	}
	public BackupImageDatabase incompletely() {
		verify(db, never()).setTransactionSuccessful();
		return this;
	}
	/** Images that are added and used for a belonging */
	public BackupImageDatabase matchedImages(String... images) throws IOException {
		for (String image : images) {
			verify(db).addImage(eq(BackupZip.getContents(image)), anyLong());
		}
		verify(db, times(images.length)).setItemImage(anyLong(), anyLong());
		return this;
	}
	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're after the data file, they should never be added, at that point we know if they're needed or not. */
	public BackupImageDatabase danglingImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, never()).addImage(eq(BackupZip.getContents(image)), anyLong());
		}
		// FIXME verify(db, never()).deleteImage(anyLong());
		return this;
	}
	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're before the data file, they may be added an then deleted, because it's not know if they're needed. */
	public BackupImageDatabase redundantImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, atMost(1)).addImage(eq(BackupZip.getContents(image)), anyLong());
		}
		verify(db, atMost(images.length)).deleteImage(anyLong());
		return this;
	}
}
