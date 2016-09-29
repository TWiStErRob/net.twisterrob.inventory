package net.twisterrob.inventory.android.test.actors;

import java.io.*;
import java.util.Locale;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.*;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.activity.CaptureImageActivityActor;
import net.twisterrob.android.test.Helpers;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public abstract class EditActivityActor extends ActivityActor {
	private static final Matcher<View> nameEditorMatcher
			= allOf(withId(R.id.title), isAssignableFrom(EditText.class));
	private static final Matcher<View> descriptionEditorMatcher
			= allOf(withId(R.id.description), isAssignableFrom(EditText.class));
	private static final Matcher<View> typeEditorMatcher
			= withId(R.id.type_edit);
	private static final Matcher<View> imageMatcher
			= allOf(withId(R.id.image), not(isDescendantOfA(Matchers.<View>instanceOf(Spinner.class))));
	private final CaptureImageActivityActor capture = new CaptureImageActivityActor();
	int cameraIntents = 0;

	public EditActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}

	public void setName(String name) {
		onView(nameEditorMatcher).perform(replaceText(name));
	}
	public void setDescription(String name) {
		onView(descriptionEditorMatcher).perform(replaceText(name));
	}
	public void checkName(String name) {
		onView(nameEditorMatcher).check(matches(withText(name)));
	}
	public void checkDescription(String name) {
		onView(descriptionEditorMatcher).check(matches(withText(name)));
	}
	public void setType(@StringRes int type) {
		onView(typeEditorMatcher).perform(click());
		String category = InstrumentationRegistry.getTargetContext().getResources().getResourceEntryName(type);
		onData(withColumn("name", category)).perform(click());
		checkType(type);
	}
	public void checkType(@StringRes int type) {
		onView(allOf(isDescendantOfA(typeEditorMatcher), withId(R.id.title)))
				.check(matches(withText(containsStringRes(type))));
	}
	public void checkPicture(@ColorInt int backgroundColor) {
		onView(imageMatcher).check(matches(withBitmap(hasBackground(backgroundColor))));
	}
	public SaveResultActor save() {
		onView(withId(R.id.btn_save)).perform(click());
		return new SaveResultActor();
	}
	public void checkDirtyDialog() {
		onView(isDialogTitle()).check(matches(allOf(
				isDisplayed(),
				withText(R.string.generic_edit_dirty_title)
		)));
	}
	public void confirmDirtyDialog() {
		checkDirtyDialog();
		clickPositiveInDialog();
	}
	public void cancelDirtyDialog() {
		checkDirtyDialog();
		clickNegativeInDialog();
	}
	public void saveFromDirtyDialog() {
		checkDirtyDialog();
		clickNeutralInDialog();
	}
	public void removePicture() {
		clickActionOverflow(R.string.action_picture_remove);
	}
	public void resetPicture() {
		clickActionOverflow(R.string.action_picture_reset);
	}
	public void takePicture(File temp, String mockBitmapText) throws IOException {
		takePicture(temp, Helpers.createMockBitmap(mockBitmapText));
	}
	public void takePicture(File temp, @ColorInt int background, String mockBitmapText) throws IOException {
		String label = String.format(Locale.ROOT, "%s\n#%08x", mockBitmapText, background);
		takePicture(temp, Helpers.createMockBitmap(background, label));
	}
	public void takePicture(File temp, Bitmap bitmap) throws IOException {
		Matcher<Intent> cameraIntent = capture.intendCamera(temp, bitmap);
		cameraIntents++;
		takePicture();
		intended(cameraIntent, times(cameraIntents));
	}
	public void takePicture() {
		//clickActionOverflow(R.string.action_picture_take);
		clickActionBar(R.id.action_picture_get);
	}

	public static class SaveResultActor extends AlertDialogActor {
		public final void checkToastAlreadyExists() {
			assertToastMessage(containsStringRes(R.string.generic_error_unique_name));
		}
	}
}
