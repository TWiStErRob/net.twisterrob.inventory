package net.twisterrob.inventory.android.test.activity;

import android.app.Activity;

/**
 * Warning: this class might be broken since upgrade to UTP / removal of sharedUserId.
 *
 * Activity that can be used to launch other activities.
 * If this one is launched with {@link android.content.Intent#FLAG_ACTIVITY_NEW_TASK}
 * the other activities started from this are not required to be launched that way, for example:
 *
 * <code><pre>
 * {@literal @}Rule public final ActivityTestRule&lt;CompatibleLauncher&gt; activity =
 * 		new TestPackageIntentRule&lt;&gt;(CompatibleLauncher.class);
 * {@literal @}Before public void launchMain() {
 * 	activity.getActivity().startActivity(new Intent...);
 * }
 * </pre></code>
 *
 * <p><i>
 * This solves a problem where a bug in the first version of Inventory prevented MainActivity from
 * starting up properly from tests, because tests required to use {@link android.content.Intent#FLAG_ACTIVITY_NEW_TASK}
 * for launching without an activity context. This activity provides a context for launching the next one.
 * </i></p>
 *
 * It's probably also useful for testing application exiting and restarting,
 * because the test keeps running even though the tested activity is destroyed.
 *
 * @see net.twisterrob.inventory.android.upgrade.UpgradeTests
 */
public class CompatibleLauncher extends Activity {
	// empty activity
}
