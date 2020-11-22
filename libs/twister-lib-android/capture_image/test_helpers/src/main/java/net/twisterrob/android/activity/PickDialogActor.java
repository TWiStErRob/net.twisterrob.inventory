package net.twisterrob.android.activity;

import androidx.annotation.RequiresApi;

import net.twisterrob.android.test.automators.UiAutomatorExtensions;

public class PickDialogActor {

	@RequiresApi(api = UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public void cancel() {
		UiAutomatorExtensions.pressBackExternal();
	}
}
