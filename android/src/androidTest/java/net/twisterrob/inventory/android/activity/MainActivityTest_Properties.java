package net.twisterrob.inventory.android.activity;

import org.junit.*;

import android.support.test.rule.ActivityTestRule;

import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.PropertiesNavigator;

import static net.twisterrob.inventory.android.content.Constants.*;

public class MainActivityTest_Properties {
	@Rule public final ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class);
	private final MainActivityActor main = new MainActivityActor();
	private PropertiesNavigator properties;

	@Before public void openProperties() {
		properties = main.openProperties();
		properties.hasNoProperties();
	}

	@Test public void testAddPropertyCancel() {
		PropertyEditActivityActor editor = properties.addProperty();
		editor.close();
		editor.assertClosing();
		properties.checkOpened();
	}

	@Test public void testAddProperty() {
		PropertyEditActivityActor editor = properties.addProperty();
		editor.setName(TEST_PROPERTY);
		editor.save();
		editor.assertClosing();
		properties.checkOpened();
		properties.hasProperty(TEST_PROPERTY);
	}
}
