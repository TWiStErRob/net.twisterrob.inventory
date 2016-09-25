package net.twisterrob.inventory.android.test.actors;

import java.io.*;
import java.util.Locale;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.*;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.runner.lifecycle.Stage;
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
import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.java.utils.ArrayTools;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class PropertyEditActivityActor {
	private static final Matcher<View> nameEditorMatcher
			= allOf(withId(R.id.title), isAssignableFrom(EditText.class));
	private static final Matcher<View> descriptionEditorMatcher
			= allOf(withId(R.id.description), isAssignableFrom(EditText.class));
	private static final Matcher<View> typeEditorMatcher
			= withId(R.id.type_edit);
	private static final Matcher<View> imageMatcher
			= allOf(withId(R.id.image), not(isDescendantOfA(Matchers.<View>instanceOf(Spinner.class))));

	private final CaptureImageActivityActor capture = new CaptureImageActivityActor();

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

	public void save() {
		onView(withId(R.id.btn_save)).perform(click());
	}

	public void rotate() {
		onView(isRoot()).perform(rotateActivity());
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
		onActionMenuView(withText(R.string.action_picture_remove)).perform(click());
	}
	public void resetPicture() {
		onActionMenuView(withText(R.string.action_picture_reset)).perform(click());
	}
	public void takePicture(File temp, String mockBitmapText) throws IOException {
		takePicture(temp, Helpers.createMockBitmap(mockBitmapText));
	}
	public void takePicture(File temp, @ColorInt int background, String mockBitmapText) throws IOException {
		String label = String.format(Locale.ROOT, "%s\n#%08x", mockBitmapText, background);
		takePicture(temp, Helpers.createMockBitmap(background, label));
	}
	int cameraIntents = 0;
	public void takePicture(File temp, Bitmap bitmap) throws IOException {
		Matcher<Intent> cameraIntent = capture.intendCamera(temp, bitmap);
		cameraIntents++;
		takePicture();
		intended(cameraIntent, times(cameraIntents));
	}

	public void takePicture() {
		//onActionMenuView(withText(R.string.action_picture_take)).perform(click());
		onView(withId(R.id.action_picture_get)).perform(click());
	}
	@SafeVarargs
	public final void checkToast(Matcher<String>... messageMatchers) {
		onView(isDialogMessage()).inRoot(isToast()).check(matches(allOf(
				isDisplayed(),
				withText(allOf(messageMatchers))
		)));
	}
	@SafeVarargs
	public final void checkToastAlreadyExists(Matcher<String>... messageMatchers) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		Matcher<String>[] specials = new Matcher[] {containsStringRes(R.string.generic_error_unique_name)};
		checkToast(ArrayTools.concat(specials, messageMatchers));
	}

	public void assertIsOpen() {
		assertThat(getActivityInStage(Stage.RESUMED), instanceOf(PropertyEditActivity.class));
	}
	public void tryClose() {
		Espresso.pressBack();
	}
}
