package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

public class DeleteDialogActor extends AlertDialogActor {
	public void cancel() {
		clickNegativeInDialog();
	}
	public void confirm() {
		clickPositiveInDialog();
	}
	public void checkDialogMessage(Matcher<String> matcher) {
		assertDialogMessage(matcher);
	}
}
