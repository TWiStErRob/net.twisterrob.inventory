package net.twisterrob.inventory.android.test

import net.twisterrob.android.test.espresso.idle.GlideResetRule
import net.twisterrob.inventory.android.BuildConfig
import net.twisterrob.inventory.android.Constants.Pic

internal class InventoryGlideResetRule : GlideResetRule() {

	override fun before() {
		super.before()
		// Recreate internal Glide wrapper.
		Pic.init(appContext, BuildConfig.VERSION_NAME)
	}

	override fun after() {
		// No need to clean up after ourselves each test will start with a clean slate,
		// let Glide continue existing after a test.
		//super.after();
	}
}
