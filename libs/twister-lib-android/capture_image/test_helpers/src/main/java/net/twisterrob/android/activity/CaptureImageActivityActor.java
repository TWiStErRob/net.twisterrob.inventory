package net.twisterrob.android.activity;

import java.io.*;

import org.hamcrest.Matcher;

import static org.junit.Assume.*;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;

import net.twisterrob.inventory.android.test.actors.ActivityActor;
import net.twisterrob.java.io.IOTools;

public class CaptureImageActivityActor extends ActivityActor {

	public CaptureImageActivityActor() {
		super(CaptureImage.class);
	}

	public Matcher<Intent> intendCamera(File file, Bitmap mockText) throws IOException {
		Uri resultFile = Uri.fromFile(file);
		OutputStream output = getContext().getContentResolver().openOutputStream(resultFile);
		try {
			assumeTrue("Cannot save bitmap to " + resultFile, mockText.compress(CompressFormat.PNG, 100, output));
		} finally {
			IOTools.ignorantClose(output);
		}
		Matcher<Intent> expectedIntent = hasComponent(CaptureImage.class.getName());
		intending(expectedIntent)
				.respondWith(new ActivityResult(Activity.RESULT_OK, new Intent(null, resultFile)));
		return expectedIntent;
	}
}
