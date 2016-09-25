package net.twisterrob.inventory.android.content;

import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.TestDatabaseRule;
import net.twisterrob.inventory.debug.test.R;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

public class DatabaseActor {
	private final TestDatabaseRule db;
	public DatabaseActor(TestDatabaseRule db) {
		this.db = db;
	}

	public void assertHasNoProperties() {
		assertThat(DatabaseTools.singleLong(App.db().stats(), "properties"), is(0L));
	}
	public void assertHasProperty(String propertyName) {
		assertThat(db.get(), hasInvProperty(propertyName));
	}
	public void assertHasNoProperty(String propertyName) {
		assertThat(db.get(), not(hasInvProperty(propertyName)));
	}

	public void assertPropertyHasDescription(String propertyName, String description) {
		assertHasProperty(propertyName);
		assertThat(getOptionalString(getProperty(propertyName), Property.DESCRIPTION), is(description));
	}

	public void assertPropertyHasType(String propertyName, @StringRes int expectedType) {
		assertPropertyHasType(propertyName, getTargetContext().getResources().getResourceEntryName(expectedType));
	}
	public void assertPropertyHasType(String propertyName, String expectedType) {
		Long expectedTypeID = db.get().getID(R.string.query_property_type_by_name, expectedType);
		assertPropertyHasType(propertyName, expectedTypeID);
	}
	public void assertPropertyHasType(String propertyName, long expectedTypeID) {
		assertHasProperty(propertyName);
		assertThat(getOptionalLong(getProperty(propertyName), Property.TYPE_ID), is(expectedTypeID));
	}
	public void assertPropertyHasImage(String propertyName) {
		assertThat(getOptionalBoolean(getProperty(propertyName), Property.HAS_IMAGE), is(true));
	}
	public void assertPropertyHasImage(String propertyName, @ColorInt int backgroundColor) {
		Cursor property = getProperty(propertyName);
		assertThat(getOptionalBoolean(property, Property.HAS_IMAGE), is(true));
		@SuppressWarnings("ConstantConditions")
		long id = DatabaseTools.singleLong(property, Property.ID);
		assertImageBackground(InventoryContract.Property.imageUri(id), backgroundColor);
	}

	private void assertImageBackground(Uri image, @ColorInt int backgroundColor) {
		InputStream stream = null;
		try {
			stream = getTargetContext().getContentResolver().openInputStream(image);
			Bitmap bitmap = BitmapFactory.decodeStream(stream);
			try {
				assertThat(bitmap.getPixel(0, 0), is(backgroundColor));
			} finally {
				bitmap.recycle();
			}
		} catch (Exception ex) {
			throw new AssertionError(ex);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}

	private Cursor getProperty(String propertyName) {
		Long id = db.get().getID(R.string.query_property_by_name, propertyName);
		assertNotNull("Property not found: " + propertyName, id);
		Cursor cursor = App.db().getProperty(id);
		try {
			return ensureFirst(DatabaseTools.clone(cursor));
		} finally {
			cursor.close();
		}
	}
	public long createProperty(String propertyName) {
		return App.db().createProperty(PropertyType.DEFAULT, propertyName, NO_DESCRIPTION);
	}
}
