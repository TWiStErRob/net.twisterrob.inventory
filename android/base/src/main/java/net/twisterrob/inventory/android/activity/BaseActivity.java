package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StrictMode;
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
	@Override public void setContentView(int layoutResID) {
		/*
		Pixel 7 Pro Android 13 / API 33
		android.os.strictmode.NonSdkApiUsedViolation:
		Landroid/view/ViewGroup;->makeOptionalFitsSystemWindows()V
		at androidx.appcompat.widget.ViewUtils.makeOptionalFitsSystemWindows(ViewUtils.java:84)
		at androidx.appcompat.app.AppCompatDelegateImpl.createSubDecor(AppCompatDelegateImpl.java:973)
		at androidx.appcompat.app.AppCompatDelegateImpl.ensureSubDecor(AppCompatDelegateImpl.java:806)
		at androidx.appcompat.app.AppCompatDelegateImpl.setContentView(AppCompatDelegateImpl.java:693)
		at androidx.appcompat.app.AppCompatActivity.setContentView(AppCompatActivity.java:170)
		at net.twisterrob.inventory.android.activity.SingleFragmentActivity.onCreate(SingleFragmentActivity.java:19)
		 */
		StrictMode.VmPolicy originalPolicy = StrictMode.getVmPolicy();
		if (Build.VERSION_CODES.P <= Build.VERSION.SDK_INT) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(originalPolicy)
					.permitNonSdkApiUsage().build());
		}
		try {
			super.setContentView(layoutResID);
		} finally {
			StrictMode.setVmPolicy(originalPolicy);
		}
	}
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

	@Override public void supportInvalidateOptionsMenu() {
		/*
		Pixel 7 Pro Android 13 / API 33
		StrictMode policy violation: android.os.strictmode.NonSdkApiUsedViolation:
		Landroid/view/View;->computeFitSystemWindows(Landroid/graphics/Rect;Landroid/graphics/Rect;)Z
		at androidx.appcompat.widget.ViewUtils.makeOptionalFitsSystemWindows(ViewUtils.java:80)
		at androidx.appcompat.app.AppCompatDelegateImpl.createSubDecor(AppCompatDelegateImpl.java:973)
		at androidx.appcompat.app.AppCompatDelegateImpl.ensureSubDecor(AppCompatDelegateImpl.java:806)
		at androidx.appcompat.app.AppCompatDelegateImpl.initWindowDecorActionBar(AppCompatDelegateImpl.java:547)
		at androidx.appcompat.app.AppCompatDelegateImpl.getSupportActionBar(AppCompatDelegateImpl.java:534)
		at androidx.appcompat.app.AppCompatDelegateImpl.invalidateOptionsMenu(AppCompatDelegateImpl.java:1217)
		at androidx.appcompat.app.AppCompatActivity.supportInvalidateOptionsMenu(AppCompatActivity.java:273)
		at androidx.fragment.app.Fragment.setHasOptionsMenu(Fragment.java:1157)
		at net.twisterrob.inventory.android.fragment.BaseFragment.onCreate(BaseFragment.java:87)
		*/
		StrictMode.VmPolicy originalPolicy = StrictMode.getVmPolicy();
		if (Build.VERSION_CODES.P <= Build.VERSION.SDK_INT) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(originalPolicy)
					.permitNonSdkApiUsage().build());
		}
		try {
			super.supportInvalidateOptionsMenu();
		} finally {
			StrictMode.setVmPolicy(originalPolicy);
		}
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
