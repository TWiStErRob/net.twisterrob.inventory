package net.twisterrob.inventory.android.test.actors;

import android.support.test.espresso.Espresso;

import net.twisterrob.android.test.espresso.DialogMatchers;

public class ChangeTypeDialogActor {
	public void assertOpen() {
		DialogMatchers.assertDialogIsDisplayed();
	}
	public void cancel() {
		Espresso.pressBack();
	}
}
