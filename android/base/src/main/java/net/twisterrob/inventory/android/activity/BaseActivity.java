package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;

import androidx.annotation.*;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.base.R;
import net.twisterrob.inventory.android.content.Intents;

import static net.twisterrob.java.utils.CollectionTools.*;

public abstract class BaseActivity extends DebugHelperActivity {
	@Override public void onContentChanged() {
		super.onContentChanged();
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		AndroidTools.showActionBarOverflowIcons(menu, true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override public void onBackPressed() {
		for (Fragment fragment : nonNull(getSupportFragmentManager().getFragments())) {
			if (fragment != null && fragment.isAdded()
					&& fragment instanceof BackPressAware && ((BackPressAware)fragment).onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	@SuppressWarnings("unchecked")
	protected <T extends Fragment> T getFragment(@IdRes int id) {
		return (T)getSupportFragmentManager().findFragmentById(id);
	}

	public void setActionBarSubtitle(CharSequence string) {
		getSupportActionBar().setSubtitle(string);
	}
	public void setActionBarTitle(CharSequence string) {
		getSupportActionBar().setTitle(string);
	}

	public void setIcon(Drawable iconDrawable) {
		getSupportActionBar().setIcon(iconDrawable);
	}
	public void setIcon(@RawRes int resourceId) {
		Pic.svg()
		   .load(resourceId)
		   .transform(new PaddingTransformation(this, ResourceTools.dipInt(this, 4)))
		   .into(new ActionBarIconTarget(getSupportActionBar()));
	}

	@SuppressWarnings("ConstantConditions")
	@Override public @NonNull ActionBar getSupportActionBar() {
		// We know that there's an action bar in all child classes.
		return super.getSupportActionBar();
	}

	@Override public boolean onSupportNavigateUp() {
		if (Intents.isChildNav(getIntent())) {
			onBackPressed();
			return true;
		}
		return super.onSupportNavigateUp();
	}

	/** Workaround for broken up navigation post Jelly Bean?!
	 * @see <a href="http://stackoverflow.com/questions/14602283/up-navigation-broken-on-jellybean">Up navigation broken on JellyBean?</a> */
	@Override public void supportNavigateUpTo(@NonNull Intent upIntent) {
		upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(upIntent);
		finish();
	}
}
