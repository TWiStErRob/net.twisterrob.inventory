package net.twisterrob.inventory.android.test.actors;

import java.io.*;
import java.util.Locale;

import org.hamcrest.Matcher;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;

import androidx.annotation.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.matcher.RootMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import net.twisterrob.android.activity.CaptureImageActivityActor;
import net.twisterrob.android.test.Helpers;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public abstract class EditActivityActor extends ActivityActor {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final Matcher<View> nameEditorMatcher
			= allOf(withId(R.id.title), isAssignableFrom(EditText.class));
	private static final Matcher<View> descriptionEditorMatcher
			= allOf(withId(R.id.description), isAssignableFrom(EditText.class));
	private static final Matcher<View> typeEditorMatcher
			= withId(R.id.type_edit);
	private static final Matcher<View> imageMatcher
			= allOf(withId(R.id.image),
			not(isDescendantOfA(isAssignableFrom(AdapterView.class))),
			not(isDescendantOfA(isAssignableFrom(RecyclerView.class)))
	);
	private static final Matcher<View> typeImageMatcher
			= allOf(withId(R.id.type),
			not(isDescendantOfA(isAssignableFrom(AdapterView.class))),
			not(isDescendantOfA(isAssignableFrom(RecyclerView.class)))
	);
	private final CaptureImageActivityActor capture = new CaptureImageActivityActor();
	private int cameraIntents = 0;

	public EditActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}

	public void setName(String name) {
		onView(nameEditorMatcher).perform(scrollTo(), replaceText(name));
	}
	public void setDescription(String name) {
		onView(descriptionEditorMatcher).perform(scrollTo(), replaceText(name));
	}
	public void checkName(String name) {
		onView(nameEditorMatcher).perform(scrollTo()).check(matches(withText(name)));
	}
	public void checkDescription(String name) {
		onView(descriptionEditorMatcher).perform(scrollTo()).check(matches(withText(name)));
	}
	public void setType(@StringRes int type) {
		onView(typeEditorMatcher).perform(scrollTo(), click());
		String typeName = ApplicationProvider.getApplicationContext().getResources().getResourceEntryName(type);
		onData(withColumn("name", typeName)).perform(click());
		if (hasRoot(isPlatformPopup())) {
			LOG.warn("Failed to select {} ({}), try again to close spinner popup.", typeName, type);
			// Not sure why it stays, but sometimes it does on API 21, and waiting doesn't help:
			// onRoot().perform(loopMainThreadForAtLeast(1000))
			onData(withColumn("name", typeName)).perform(click());
		}
		// Faster version of Espresso expression below.
		assertFalse("Spinner popup should be closed", hasRoot(isPlatformPopup()));
		//onRoot(isPlatformPopup()) // takes a long time waiting for non-existent root
		//		.withFailureHandler(new PassMissingRoot())
		//		.check(matches(not(anything("popup root existed"))));
		checkType(type);
	}
	public void checkType(@StringRes int type) {
		onView(typeEditorMatcher).perform(scrollTo());
		onView(allOf(isDescendantOfA(typeEditorMatcher), withId(R.id.title)))
				.check(matches(withText(containsStringRes(type))));
	}
	public void checkPicture(@ColorInt int backgroundColor) {
		onView(imageMatcher).perform(scrollTo()).check(matches(withBitmap(hasBackgroundColor(backgroundColor))));
	}
	public InfoPopupActor help() {
		onView(withId(R.id.help)).perform(click());
		InfoPopupActor popup = new InfoPopupActor();
		popup.assertDisplayed();
		return popup;
	}
	public SaveResultActor save() {
		onView(withId(R.id.btn_save)).perform(click());
		return new SaveResultActor();
	}
	public void checkDirtyDialog() {
		onView(isDialogTitle())
				.check(matches(isCompletelyDisplayed()))
				.check(matches(withText(R.string.generic_edit_dirty_title)))
		;
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
		onView(imageMatcher).perform(scrollTo());
		clickActionOverflow(R.id.action_picture_remove);
	}
	public void resetPicture() {
		onView(imageMatcher).perform(scrollTo());
		clickActionOverflow(R.id.action_picture_reset);
	}
	public void takePicture(File temp, String mockBitmapText) throws IOException {
		takePicture(temp, Helpers.createMockBitmap(mockBitmapText));
	}
	public void takePicture(File temp, @ColorInt int background, String mockBitmapText) throws IOException {
		String label = String.format(Locale.ROOT, "%s\n#%08x", mockBitmapText, background);
		takePicture(temp, Helpers.createMockBitmap(background, label));
	}
	public void takePicture(File temp, Bitmap bitmap) throws IOException {
		onView(imageMatcher).perform(scrollTo());
		Matcher<Intent> cameraIntent = capture.intendCamera(temp, bitmap);
		cameraIntents++;
		takePicture();
		intended(cameraIntent, times(cameraIntents));
	}
	public void takePicture() {
		//clickActionOverflow(R.string.action_picture_take);
		clickActionBar(R.id.action_picture_get);
	}

	public ChangeTypeDialogActor changeType() {
		onView(typeImageMatcher).perform(scrollTo(), click());
		ChangeTypeDialogActor actor = new ChangeTypeDialogActor();
		actor.assertOpen();
		return actor;
	}

	public static class SaveResultActor extends AlertDialogActor {
		public final void checkToastAlreadyExists() {
			Matcher<String> matcher = containsStringRes(R.string.generic_error_unique_name);
			assertToastMessage(withText(matcher));
		}
	}

	public static class InfoPopupActor {
		public void assertDisplayed() {
			onRoot(isPlatformPopup()).check(matches(isCompletelyDisplayed()));
		}

		public KeywordsDialogActor showKeywords() {
			onData(withMenuItemId(R.id.action_category_keywords)).perform(click());
			KeywordsDialogActor dialog = new KeywordsDialogActor();
			dialog.assertDisplayed();
			return dialog;
		}

		public CategoryActivityActor jumpToCategory() {
			onData(withMenuItemId(R.id.action_category_goto)).perform(click());
			CategoryActivityActor activity = new CategoryActivityActor();
			activity.assertIsInFront();
			return activity;
		}

		public void suggestCategories() {
			onData(withMenuItemId(R.id.action_category_suggest)).perform(click());
		}
	}
}
