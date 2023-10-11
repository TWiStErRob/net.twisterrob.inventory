package net.twisterrob.inventory.android.view;

import java.util.Locale;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;
import android.view.*;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import androidx.annotation.*;

import net.twisterrob.android.content.glide.MenuItemTarget;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;

public class DrawerNavigator {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerNavigator.class);

	private final NavigationView nav;
	private final Activity activity;
	private final SparseArray<NavItem> items = new SparseArray<>();
	private final int iconSize;
	private OnNavigationItemSelectedListener navigationItemSelectedListener;

	public DrawerNavigator(NavigationView nav, Activity activity) {
		this.nav = nav;
		this.activity = activity;
		this.iconSize = activity.getResources().getDimensionPixelSize(R.dimen.design_navigation_icon_size);
		nav.setTag(this);
		nav.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			@Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				boolean result = navigationItemSelectedListener.onNavigationItemSelected(item);
				return result || trigger(item.getItemId());
			}
		});
	}

	public void setNavigationItemSelectedListener(OnNavigationItemSelectedListener navigationItemSelectedListener) {
		this.navigationItemSelectedListener = navigationItemSelectedListener;
	}

	public void add(@IdRes int id, @RawRes int icon, Intent intent) {
		items.put(id, new NavItem(id, icon, intent));
	}

	public boolean trigger(@IdRes int id) {
		NavItem item = items.get(id);
		if (item != null) {
			activity.startActivity(item.intent);
			return true;
		} else {
			return false;
		}
	}

	public @IdRes int find(Intent intent) {
		for (int pos = 0; pos < items.size(); pos++) {
			NavItem item = items.get(items.keyAt(pos));
			if (intent.getComponent().equals(item.intent.getComponent())
					&& AndroidTools.equals(intent.getExtras(), item.intent.getExtras())) {
				return item.id;
			}
		}
		return View.NO_ID;
	}

	public void addIcons() {
		// app:itemIconTint="@null" triggers !hasValue in NavigationView constructor, which creates default.
		nav.setItemIconTintList(null);
		Menu menu = nav.getMenu();
		for (int i = 0; i < menu.size(); ++i) {
			loadIcon(menu.getItem(i));
		}
	}

	private void loadIcon(final MenuItem menuItem) {
		final NavItem navItem = items.get(menuItem.getItemId());
		if (navItem == null) {
			if (BuildConfig.DEBUG) {
				LOG.warn("{} has no data defined.", StringerTools.toNameString(activity, menuItem.getItemId()));
			}
			return;
		}
		Pic.svg().load(navItem.icon).override(iconSize).into(new MenuItemTarget(menuItem));
	}

	public static DrawerNavigator get(View nav) {
		return (DrawerNavigator)nav.getTag();
	}

	public void select(Intent intent) {
		int id = find(intent);
		nav.setCheckedItem(id);
	}
	public void updateCounts() {
		AndroidTools.executePreferParallel(new DrawerUpdateCountersTask(nav));
	}

	private class NavItem {
		private final @IdRes int id;
		private final @RawRes int icon;
		private final Intent intent;
		NavItem(@IdRes int id, @RawRes int icon, Intent intent) {
			this.id = id;
			this.icon = icon;
			this.intent = intent;
		}
		@Override public @NonNull String toString() {
			if (BuildConfig.DEBUG) {
				return String.format(Locale.ROOT, "[%s] %s: %s",
						activity.getResources().getResourceName(icon),
						activity.getResources().getResourceName(id),
						StringerTools.toString(intent)
				);
			} else {
				return super.toString();
			}
		}
	}
}
