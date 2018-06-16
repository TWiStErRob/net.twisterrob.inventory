package net.twisterrob.inventory.android.backup;

import java.io.IOException;
import java.util.*;

import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

import android.annotation.SuppressLint;

import com.shazam.gwen.collaborators.Asserter;

import net.twisterrob.inventory.android.content.Database;

@SuppressWarnings("UnusedReturnValue")
public class BackupImageDatabase implements Asserter {
	private final Database db;
	private long generatedImageId = 100;
	private Map<String, Long> imageIds = new HashMap<>();
	@SuppressLint("UseSparseArrays") // this runs on desktop, LongSparseArray is not available
	private Map<Long, byte[]> imageContents = new HashMap<>();
	@SuppressLint("UseSparseArrays") // this runs on desktop, LongSparseArray is not available
	private Map<Long, Long> itemImages = new HashMap<>();
	public BackupImageDatabase(Database mock) {
		db = mock;
		Answer<Long> insertImage = new Answer<Long>() {
			@Override public Long answer(InvocationOnMock invocation) throws Throwable {
				byte[] contents = invocation.getArgument(0);
				assertThat(imageContents, not(hasValue(contents)));
				imageContents.put(generatedImageId, contents);
				imageIds.put(BackupZip.getName(contents), generatedImageId);
				return generatedImageId++; // generate a unique ID for this run
			}
		};
		Answer<Void> deleteImage = new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) {
				long imageId = invocation.getArgument(0);
				assertThat(imageContents, hasKey(imageId));
				imageContents.remove(imageId);
				return null;
			}
		};
		Answer<Void> associateImage = new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) {
				long id = invocation.getArgument(0);
				long imageId = invocation.getArgument(1);
				assertThat(itemImages, not(hasKey(id)));
				assertThat(itemImages, not(hasValue(imageId)));
				itemImages.put(id, imageId);
				return null;
			}
		};
		doAnswer(insertImage).when(db).addImage(any(byte[].class), ArgumentMatchers.<Long>any());
		doAnswer(associateImage).when(db).setItemImage(anyLong(), anyLong());
		doAnswer(deleteImage).when(db).deleteImage(anyLong());
	}

	/** Images that are added and used for a belonging */
	public BackupImageDatabase matchedImages(String... images) throws IOException {
		for (String image : images) {
			verify(db)
					.addImage(aryEq(BackupZip.getContents(image)), ArgumentMatchers.<Long>any());
			verify(db)
					.setItemImage(anyLong(), eq(imageIds.get(image)));
		}
		return this;
	}

	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're after the data file, they should never be added, at that point we know if they're needed or not. */
	public BackupImageDatabase danglingImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, never())
					.addImage(aryEq(BackupZip.getContents(image)), any(Long.class));
			//verify(db, never()).deleteImage(eq(imageIds.get(image))); // can't verify, if it wasn't added, there's no id
			verify(db, never())
					.setItemImage(anyLong(), eq(imageIds.get(image)));
		}
		return this;
	}

	/** Images that are in the ZIP file, but never used for any belonging.
	 * If they're before the data file, they may be added an then deleted, because it's not know if they're needed. */
	public BackupImageDatabase redundantImages(String... images) throws IOException {
		for (String image : images) {
			verify(db, atMost(1))
					.addImage(aryEq(BackupZip.getContents(image)), ArgumentMatchers.<Long>any());
			verify(db, atMost(1))
					.deleteImage(imageIds.get(image));
		}
		return this;
	}
}
