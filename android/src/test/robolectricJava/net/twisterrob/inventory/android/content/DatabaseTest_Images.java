package net.twisterrob.inventory.android.content;

import java.util.*;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.slf4j.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.Cursor;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.TestIgnoreApp;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.test.frameworks.RobolectricTestBase;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(application = TestIgnoreApp.class)
public class DatabaseTest_Images extends RobolectricTestBase {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseTest_Images.class);
	public static final byte[] IMAGE_CONTENTS = new byte[] {1, 2, 3};
	private final String name;
	private final Belonging belonging;
	private Database database;
	private ThreadPolicy originalPolicy;

	abstract static class Belonging {
		Database db;
		abstract long create();
		abstract Cursor getImage(long id);
		abstract Cursor get(long id);
		abstract void delete(long id);
		abstract void setImage(long id, Long imageID);
	}

	public DatabaseTest_Images(String name, Belonging belonging) {
		this.name = name;
		this.belonging = belonging;
	}

	@Before public void setUp() {
		database = new Database(RuntimeEnvironment.application);
		originalPolicy = StrictMode.allowThreadDiskWrites();
		database.getWritableDatabase();
		belonging.db = database;
	}
	@After public void tearDown() {
		try {
			Cursor cursor = database.getWritableDatabase().rawQuery("select * from Log;", NO_ARGS);
			while (cursor.moveToNext()) {
				LOG.trace("{} {}: {}", getLong(cursor, "_id"), getString(cursor, "at"), getString(cursor, "message"));
			}
			cursor.close();
		} catch (Exception ex) {
			LOG.warn("Cannot get Log table", ex);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
			// otherwise XStream wants to serialize the whole App object because DeepCloner is in methodBlock()
			belonging.db = null;
		}
	}
	private long createDefaultImage() {
		return database.addImage(IMAGE_CONTENTS, null);
	}

	@Test public void testCreateWithoutImage() {
		assertNoImages();
		long id = belonging.create();
		assertHasImage(id, is(false));
		assertNoImages();
	}

	@Test public void testCreateWithImage() {
		long id = belonging.create();
		long imageID = createDefaultImage();
		assertSingleImage();
		belonging.setImage(id, imageID);
		assertSingleImage();
		assertHasImage(id, is(true));
		assertImageContents(id);
	}

	@Test public void testUnsetImage() {
		long id = belonging.create();
		long imageID = createDefaultImage();
		belonging.setImage(id, imageID);

		belonging.setImage(id, null);
		assertHasImage(id, is(false));
		assertNoImages();
	}

	@Test public void testDeleteWithoutImage() {
		long id = belonging.create();

		belonging.delete(id);
		assertNoImages();
	}

	@Test public void testDeleteWithImage() {
		long id = belonging.create();
		long imageID = createDefaultImage();
		belonging.setImage(id, imageID);

		assertSingleImage();
		belonging.delete(id);
		assertNoImages();
	}

	/** ON DELETE SET DEFAULT */
	@Test public void testDeleteImage() {
		long id = belonging.create();
		long imageID = createDefaultImage();
		belonging.setImage(id, imageID);

		database.deleteImage(imageID);
		assertNoImages();
		assertHasImage(id, is(false));
	}

	@Test public void testDeleteAll() {
		long id = belonging.create();
		long imageID = createDefaultImage();
		belonging.setImage(id, imageID);

		try (Cursor properties = database.listProperties()) {
			while (properties.moveToNext()) {
				database.deleteProperty(DatabaseTools.getLong(properties, Property.ID));
			}
		}
		assertNoImages();
	}

	private void assertImageContents(long id) {
		try (Cursor image = belonging.getImage(id)) {
			assertThat("image.count", image.getCount(), is(1));
			assertTrue(image.moveToFirst());
			assertThat(getBlob(image, ImageDataColumns.COLUMN_BLOB), is(IMAGE_CONTENTS));
		}
	}
	private void assertHasImage(long id, Matcher<Boolean> matcher) {
		try (Cursor belongingData = belonging.get(id)) {
			assertThat(name + ".count", belongingData.getCount(), is(1));
			assertTrue(belongingData.moveToFirst());
			assertThat(getBoolean(belongingData, Property.HAS_IMAGE), matcher);
		}
	}

	private void assertNoImages() {
		assertImageCount(is(0));
	}
	private void assertSingleImage() {
		assertImageCount(is(1));
	}
	private void assertImageCount(Matcher<Integer> imageCount) {
		try (Cursor images = database.getWritableDatabase().rawQuery("select * from Image;", NO_ARGS)) {
			assertThat("images.count", images.getCount(), imageCount);
		}
	}

	@ParameterizedRobolectricTestRunner.Parameters(name = "{0}") public static List<Object[]> data() {
		return Arrays.asList(
				new Object[] {"property", new Belonging() {
					@Override public long create() {
						return db.createProperty(PropertyType.DEFAULT, "Test Property", null);
					}
					@Override public Cursor get(long id) {
						return db.getProperty(id);
					}
					@Override public void delete(long id) {
						db.deleteProperty(id);
					}
					@Override public Cursor getImage(long id) {
						return db.getPropertyImage(id);
					}
					@Override public void setImage(long id, Long imageID) {
						db.setPropertyImage(id, imageID);
					}
				}},
				new Object[] {"room", new Belonging() {
					@Override long create() {
						long propertyID = db.createProperty(PropertyType.DEFAULT, "Test Room Property", null);
						return db.createRoom(propertyID, RoomType.DEFAULT, "Test Room", null);
					}
					@Override public Cursor get(long id) {
						return db.getRoom(id);
					}
					@Override public void delete(long id) {
						db.deleteRoom(id);
					}
					@Override public Cursor getImage(long id) {
						return db.getRoomImage(id);
					}
					@Override public void setImage(long id, Long imageID) {
						db.setRoomImage(id, imageID);
					}
				}},
				new Object[] {"item", new Belonging() {
					@Override long create() {
						long propertyID = db.createProperty(PropertyType.DEFAULT, "Test Item Property", null);
						long roomID = db.createRoom(propertyID, RoomType.DEFAULT, "Test Item Room", null);
						//noinspection ConstantConditions NPE on unbox, so be it, test should fail anyway in that case
						long rootID = DatabaseTools.singleLong(db.getRoom(roomID), Room.ROOT_ITEM);
						return db.createItem(rootID, Category.DEFAULT, "Test Item", null);
					}
					@Override public Cursor get(long id) {
						return db.getItem(id, false);
					}
					@Override public void delete(long id) {
						db.deleteItem(id);
					}
					@Override public Cursor getImage(long id) {
						return db.getItemImage(id);
					}
					@Override public void setImage(long id, Long imageID) {
						db.setItemImage(id, imageID);
					}
				}},
				new Object[] {"sub-item", new Belonging() {
					@Override long create() {
						long propertyID = db.createProperty(PropertyType.DEFAULT, "Test Sub Item Property", null);
						long roomID = db.createRoom(propertyID, RoomType.DEFAULT, "Test Sub Item Room", null);
						//noinspection ConstantConditions NPE on unbox, so be it, test should fail anyway in that case
						long rootID = DatabaseTools.singleLong(db.getRoom(roomID), Room.ROOT_ITEM);
						long parentID = db.createItem(rootID, Category.DEFAULT, "Test Sub Item Parent", null);
						return db.createItem(parentID, Category.DEFAULT, "Test Sub Item Child", null);
					}
					@Override public Cursor get(long id) {
						return db.getItem(id, false);
					}
					@Override public void delete(long id) {
						db.deleteItem(id);
					}
					@Override public Cursor getImage(long id) {
						return db.getItemImage(id);
					}
					@Override public void setImage(long id, Long imageID) {
						db.setItemImage(id, imageID);
					}
				}}
		);
	}
}
