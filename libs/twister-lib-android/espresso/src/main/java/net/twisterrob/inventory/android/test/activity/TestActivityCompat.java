package net.twisterrob.inventory.android.test.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;

/**
 * Generic empty test activity in case a UI context is needed for the tests.
 * If anything more fancy is required, like a different theme or some views,
 * create an inner class inside the test and register it in the manifest.
 */
public class TestActivityCompat extends AppCompatActivity {
	@SuppressLint("PrivateResource") // we're a test app for that app, so accessing private should be ok
	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		setTheme(net.twisterrob.android.test.espresso.R.style.Theme_AppCompat);
		super.onCreate(savedInstanceState);
	}

	/**
	 * Override the base context to use the tested app's context,
	 * this makes resources like app compat themes and styles available.
	 * Be careful if you want to inflate a view from test app's APK into this activity!
	 */
	@Override protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(ApplicationProvider.getApplicationContext());
	}
}
