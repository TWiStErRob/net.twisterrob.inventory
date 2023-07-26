package net.twisterrob.inventory.android.test;

import androidx.test.core.app.ApplicationProvider;

import net.twisterrob.android.test.espresso.idle.GlideResetRule;
import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.inventory.android.Constants;

class InventoryGlideResetRule extends GlideResetRule {
   @Override protected void before() {
      super.before();
      // recreate internal Glide wrapper
      Constants.Pic.init(ApplicationProvider.getApplicationContext(), BuildConfig.VERSION_NAME);
   }

   @Override protected void after() {
      // No need to clean up after ourselves each test will start with a clean slate,
      // let Glide continue existing after a test.
      //super.after();
   }
}
