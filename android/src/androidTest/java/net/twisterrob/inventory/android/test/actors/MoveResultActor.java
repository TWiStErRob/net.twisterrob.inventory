package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class MoveResultActor extends AlertDialogActor {
	public void cancel() {
		clickNegativeInDialog();
	}
	public void confirm() {
		clickPositiveInDialog();
	}
	public void checkDialogMessage(Matcher<String> matcher) {
		assertDialogMessage(matcher);
	}
	public void checkToastMessage(Matcher<String> matcher) {
		assertToastMessage(matcher);
	}
	public void checkToastMessageDuplicate(Matcher<String> matcher) {
		checkToastMessage(allOf(containsStringRes(R.string.generic_error_unique_name), matcher));
	}
}
