package net.twisterrob.android.activity;

import java.io.*;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Bitmap.*;
import android.graphics.drawable.*;
import android.hardware.Camera;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.*;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;
import android.widget.ImageView;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.capture_image.R;
import net.twisterrob.inventory.android.test.actors.ActivityActor;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.android.test.automators.AndroidAutomator.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

/**
 * @see CaptureImage
 */
public class CaptureImageActivityActor extends ActivityActor {

	public CaptureImageActivityActor() {
		super(CaptureImage.class);
	}

	public Matcher<Intent> intendCamera(File file, Bitmap mockText) throws IOException {
		Uri resultFile = Uri.fromFile(file);
		OutputStream output = getContext().getContentResolver().openOutputStream(resultFile);
		try {
			boolean compressed = mockText.compress(CompressFormat.PNG, 100, output);
			assumeTrue("Cannot save bitmap to " + resultFile, compressed);
		} finally {
			IOTools.ignorantClose(output);
		}
		Matcher<Intent> expectedIntent = hasComponent(CaptureImage.class.getName());
		intending(expectedIntent)
				.respondWith(new ActivityResult(Activity.RESULT_OK, new Intent(null, resultFile)));
		return expectedIntent;
	}

	@SuppressLint("ApplySharedPref") // want to save persist immediately
	public void clearPreferences() {
		InstrumentationRegistry
				.getTargetContext()
				.getSharedPreferences(CaptureImage.class.getName(), Context.MODE_PRIVATE)
				.edit()
				.clear()
				.commit()
		;
	}

	public void allowPermissions() throws UiObjectNotFoundException {
		allowPermissionsIfNeeded();
	}

	public void assumeHasCamera() {
		assumeThat(Camera.getNumberOfCameras(), greaterThan(0));
	}

	public void assumeCameraHasFlash() {
		Camera camera = Camera.open(0);
		try {
			assumeThat(camera.getParameters().getSupportedFlashModes(), hasItems(
					Camera.Parameters.FLASH_MODE_ON,
					Camera.Parameters.FLASH_MODE_OFF
			));
		} finally {
			camera.release();
		}
	}

	public void assertFlashOn() {
		// TODO check drawable
		onView(withId(R.id.btn_flash)).check(matches(isChecked()));
	}

	public void assertFlashOff() {
		// TODO check drawable
		onView(withId(R.id.btn_flash)).check(matches(isNotChecked()));
	}

	public void turnFlashOn() {
		onView(withId(R.id.btn_flash)).perform(checkIfUnchecked());
	}

	public void turnFlashOff() {
		onView(withId(R.id.btn_flash)).perform(uncheckIfChecked());
	}

	public PickDialogActor pick() {
		onView(withId(R.id.btn_pick)).perform(click());
		return new PickDialogActor();
	}

	public void take() {
		onView(withId(R.id.btn_capture)).perform(click());
	}

	public void crop() {
		onView(withId(R.id.btn_crop)).perform(click());
	}

	public Uri createFakeImage(File fakeFile, @ColorInt int color) throws IOException {
		Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
		bitmap.setPixel(0, 0, color);
		FileOutputStream stream = new FileOutputStream(fakeFile);
		try {
			bitmap.compress(CompressFormat.PNG, 100, stream);
		} finally {
			stream.close();
		}
		return Uri.fromFile(fakeFile);
	}

	public void verifyNoImage() {
		onView(withId(R.id.image)).check(matches(hasBitmap(nullValue())));
	}

	public void verifyImageColor(Matcher<? super Integer> colorMatcher) {
		onView(withId(R.id.image)).check(matches(hasBitmap(withPixelAt(0, 0, colorMatcher))));
	}

	public void verifyErrorImage() {
		// TODO hasDrawable(R.drawable.image_error)
		Matcher<Object> errorDrawable = allOf(
				instanceOf(LayerDrawable.class), // contains two layers of RotateDrawables
				not(instanceOf(TransitionDrawable.class)) // not glide animation
		);
		onView(withId(R.id.image)).check(matches(hasDrawable(errorDrawable)));
	}

	@SuppressWarnings("unchecked")
	private Matcher<View> hasDrawable(Matcher<? super Drawable> drawableMatcher) {
		return (Matcher<View>)(Matcher<?>)new FeatureMatcher<ImageView, Drawable>(
				drawableMatcher, "drawable", "drawable") {
			@Override protected Drawable featureValueOf(ImageView actual) {
				return actual.getDrawable();
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static Matcher<View> hasBitmap(final Matcher<? super Bitmap> bitmapMatcher) {
		return (Matcher<View>)(Matcher<?>)new FeatureMatcher<ImageView, Bitmap>(
				bitmapMatcher, "bitmap in drawable", "bitmap") {
			@Override protected Bitmap featureValueOf(ImageView actual) {
				Drawable drawable = actual.getDrawable();
				if (drawable == null) {
					return null;
				}
				if (drawable instanceof LayerDrawable) {
					// In case Glide is animating from thumbnail (0) to main result (1), use result.
					drawable = ((LayerDrawable)drawable).getDrawable(1);
				}
				return ((BitmapDrawable)drawable).getBitmap();
			}
		};
	}

	private static Matcher<Bitmap> withPixelAt(
			final int x, final int y, Matcher<? super Integer> colorMatcher) {
		return new FeatureMatcher<Bitmap, Integer>(
				colorMatcher, "pixel at " + x + ", " + y, "pixel color") {
			@Override protected Integer featureValueOf(Bitmap actual) {
				return actual.getPixel(x, y);
			}
		};
	}

	public void intendExternalChooser(Uri fakeUri) {
		Intents.intending(hasAction(Intent.ACTION_CHOOSER))
		       .respondWith(new ActivityResult(Activity.RESULT_OK, new Intent().setData(fakeUri)));
	}

	public void intendExternalChooserCancelled() {
		Intents.intending(hasAction(Intent.ACTION_CHOOSER))
		       .respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));
	}

	public void verifyExternalChooser() {
		verifyExternalChooser(times(1));
	}
	public void verifyExternalChooser(VerificationMode mode) {
		String title = InstrumentationRegistry.getTargetContext()
		                                      .getString(R.string.image__choose_external__title);
		Intents.intended(allOf(
				hasAction(Intent.ACTION_CHOOSER),
				hasExtra(Intent.EXTRA_TITLE, title)
		), mode);
	}
}
