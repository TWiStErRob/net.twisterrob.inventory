package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.PropertiesNavigator;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Property.class})
public class MainActivityTest_Properties {

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<MainActivity> activity =
			new InventoryActivityRule<>(MainActivity.class);

	private final MainActivityActor main = new MainActivityActor();
	private PropertiesNavigator properties;

	@Before public void openProperties() {
		properties = main.openProperties();
		properties.hasNoProperties();
	}

	@Category({Op.Cancels.class})
	@Test public void testAddPropertyCancel() {
		PropertyEditActivityActor editor = properties.addProperty();
		editor.close();
		editor.assertClosing();
		properties.checkOpened();
	}

	@Category({Op.CreatesBelonging.class})
	@Test public void testAddProperty() {
		PropertyEditActivityActor editor = properties.addProperty();
		editor.setName(TEST_PROPERTY);
		editor.save();
		editor.assertClosing();
		properties.checkOpened();
		properties.hasProperty(TEST_PROPERTY);
	}
}
