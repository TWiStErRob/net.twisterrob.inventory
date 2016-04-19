package net.twisterrob.inventory.android.view;

import java.util.Locale;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.support.annotation.*;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.SparseArray;
import android.view.*;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;

public class DrawerNavigator {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerNavigator.class);

	private final NavigationView nav;
	private final Activity activity;
	private final SparseArray<NavItem> items = new SparseArray<>();
	private final int iconSize;
	private OnNavigationItemSelectedListener navigationItemSelectedListener;

	@SuppressLint("PrivateResource") // TODEL https://b.android.com/207152 overridden resource
	public DrawerNavigator(NavigationView nav, Activity activity) {
		this.nav = nav;
		this.activity = activity;
		this.iconSize = activity.getResources().getDimensionPixelSize(R.dimen.design_navigation_icon_size);
		nav.setTag(this);
		nav.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			@Override public boolean onNavigationItemSelected(MenuItem item) {
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
					&& AndroidTools.toString(intent.getExtras())
					               .equals(AndroidTools.toString(item.intent.getExtras()))) {
				return item.id;
			}
		}
		return View.NO_ID;
	}

	public void addIcons() {
		nav.setItemIconTintList(null);
		Menu menu = nav.getMenu();
		for (int i = 0; i < menu.size(); ++i) {
			loadIcon(menu.getItem(i));
		}
	}

	private void loadIcon(final MenuItem menuItem) {
		final NavItem navItem = items.get(menuItem.getItemId());
		if (navItem == null) {
			LOG.warn("{} has no data defined.", AndroidTools.toNameString(activity, menuItem.getItemId()));
			return;
		}
		Pic.svg().load(navItem.icon).into(new MenuItemTarget(menuItem, iconSize));
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
		@Override public String toString() {
			return String.format(Locale.ROOT, "[%s] %s: %s",
					activity.getResources().getResourceName(icon),
					activity.getResources().getResourceName(id),
					AndroidTools.toString(intent)
			);
		}
	}

	private static class MenuItemTarget extends SimpleTarget<GlideDrawable> {
		private final MenuItem menuItem;
		public MenuItemTarget(MenuItem menuItem, int iconSize) {
			super(iconSize, iconSize);
			this.menuItem = menuItem;
		}
		@Override
		public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
			resource.setColorFilter(Pic.TINT_FILTER);
			menuItem.setIcon(new DrawableWrapper(resource) {
				@Override public void setColorFilter(ColorFilter cf) {
					//super.setColorFilter(cf); // don't call
					// when the item's icon is used in navigation view:
					// android.support.design.internal.NavigationMenuItemView.setIcon()
					// it'll wrap the drawable for tinting and set tintList (which is null, see #addIcons)
					// DrawableCompat.setTintList(icon, mIconTintList);
					// android.support.v4.graphics.drawable.DrawableWrapperDonut.setCompatTintList()
					// it finally reaches the updateTint method which clears the color filter:
					// if (tintList == null || tintMode == null) clearColorFilter();
					// which will propagate back to resource.setColorFilter(null), because it is wrapped.
					// Workaround: Wrap it in another layer which ignores that call.
				}
			});
		}
	}
}
