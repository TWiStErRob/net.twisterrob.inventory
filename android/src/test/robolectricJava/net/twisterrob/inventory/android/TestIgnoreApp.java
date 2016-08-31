package net.twisterrob.inventory.android;

import android.app.Application;

/**
 * Dummy {@link Application} class to suppress any static initialization done by {@link App}.
 * @see org.robolectric.RobolectricTestRunner @Config(application = TestIgnoreApp.class)
 */
public class TestIgnoreApp extends Application {
}
