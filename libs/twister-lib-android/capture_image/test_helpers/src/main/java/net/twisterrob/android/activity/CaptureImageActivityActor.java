package net.twisterrob.android.activity;

import java.io.*;

import org.hamcrest.Matcher;

import static org.junit.Assume.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiObjectNotFoundException;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.capture_image.test_helpers.R;
import net.twisterrob.inventory.android.test.actors.ActivityActor;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.android.test.automators.AndroidAutomator.*;

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

	public void assertFlashOn() {
		// TODO check drawable
		onView(withId(R.id.btn_flash)).check(matches(isChecked()));
	}

	public void assertFlashOff() {
		// TODO check drawable
		onView(withId(R.id.btn_flash)).check(matches(isNotChecked()));
	}

	public void turnFlashOn() {
		assertFlashOff();
		onView(withId(R.id.btn_flash)).perform(click());
		assertFlashOn();
	}

	public void turnFlashOff() {
		assertFlashOn();
		onView(withId(R.id.btn_flash)).perform(click());
		assertFlashOff();
	}
}
