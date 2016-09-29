package net.twisterrob.inventory.android.activity;

import org.junit.*;

import android.support.test.espresso.*;
import android.support.test.rule.ActivityTestRule;

import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.*;

public class MainActivityTest_Drawer {
	@Rule public final ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class);
	private final MainActivityActor main = new MainActivityActor();

	@Before public void startup() {
		main.assertHomeScreen();
	}

	@Test(expected = NoActivityResumedException.class)
	public void testHomeBack() {
		main.assertHomeScreen().tryClose();
	}

	@Test public void testDrawerBack() {
		main.openMenu();
		Espresso.pressBack();
		main.assertMenuClosed();
	}

	@Test public void testCategories() {
		main.openCategories();
	}
	@Test public void testProperties() {
		main.openProperties();
	}
	@Test public void testRooms() {
		main.openRooms();
	}
	@Test public void testItems() {
		main.openItems();
	}
	@Test public void testSunburst() {
		main.openSunburst();
	}
	@Test public void testBackup() {
		main.openBackup();
	}

	@Test public void testBackupAndBack() {
		{
			BackupActivityActor backup = main.openBackup().asActor();
			backup.close();
			backup.assertClosing();
		}
		main.assertHomeScreen();
	}
	@Test public void testBackupAndBackDeep() {
		{
			PropertiesNavigator properties = main.openProperties();
			{
				BackupActivityActor backup = main.openBackup().asActor();
				backup.close();
			}
			properties.checkOpened();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}

	@Test public void testBackNavigation_Single() {
		{
			PropertiesNavigator properties = main.openProperties();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}

	@Test public void testBackNavigation_Multiple() {
		{
			PropertiesNavigator properties = main.openProperties();
			{
				RoomsNavigator rooms = main.openRooms();
				main.openItems().tryClose();
				rooms.checkOpened();
				rooms.tryClose();
			}
			properties.checkOpened();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}
	@Test public void testBackNavigation_BackAndForth() {
		{
			PropertiesNavigator properties = main.openProperties();
			main.openRooms().tryClose();
			properties.checkOpened();
			main.openItems().tryClose();
			properties.checkOpened();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}

	@Test public void testRotation() {
		PropertiesNavigator properties = main.openProperties();
		main.rotate();
		properties.checkOpened();
	}
	@Test public void testBackNavigation_Rotation() {
		{
			PropertiesNavigator properties = main.openProperties();
			main.rotate();
			properties.checkOpened();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}
	@Test public void testBackNavigation_RotationComplex() {
		{
			PropertiesNavigator properties = main.openProperties();
			{
				RoomsNavigator rooms = main.openRooms();
				main.rotate();
				rooms.checkOpened();
				{
					ItemsNavigator items = main.openItems();
					main.rotate();
					items.checkOpened();
					items.tryClose();
				}
				rooms.checkOpened();
				rooms.tryClose();
			}
			properties.checkOpened();
			properties.tryClose();
		}
		main.assertHomeScreen();
	}
}
