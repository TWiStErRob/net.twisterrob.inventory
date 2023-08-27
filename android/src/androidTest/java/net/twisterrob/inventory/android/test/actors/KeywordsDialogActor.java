package net.twisterrob.inventory.android.test.actors;

import static org.hamcrest.Matchers.anyOf;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.test.espresso.Espresso;

import static androidx.test.espresso.matcher.ViewMatchers.withText;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.ChangeTypeDialog;

import static net.twisterrob.android.test.matchers.AndroidMatchers.formattedRes;

/**
 * @see ChangeTypeDialog#showKeywords(Context, CharSequence, CharSequence)
 */
public class KeywordsDialogActor extends AlertDialogActor {
	@Override public void assertDisplayed() {
		assertDialogTitle(anyOf(
				withText(formattedRes(R.string.category_keywords_of)),
				withText(R.string.category_keywords_empty)
		));
	}

	public void assertKeywords(@StringRes int keywords) {
		assertDialogMessage(withText(keywords));
	}

	public void close() {
		// Could also "tap outside", but not sure how to do that.
		Espresso.pressBack();
	}
}
