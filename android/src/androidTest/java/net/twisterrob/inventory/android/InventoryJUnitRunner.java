package net.twisterrob.inventory.android;

import android.os.Bundle;

import net.twisterrob.android.test.junit.AndroidJUnitRunner;
import net.twisterrob.inventory.android.hacks.ViewCompatHacks;

public class InventoryJUnitRunner extends AndroidJUnitRunner {
	@Override public void onCreate(Bundle arguments) {
		super.onCreate(arguments);
		ViewCompatHacks.patchFor293190504();
	}
}
