package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.*;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.widget.TextView;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.*;

public class DrawerUpdateCountersTask extends SimpleSafeAsyncTask<Void, Void, DrawerUpdateCountersTask.Stats> {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerUpdateCountersTask.class);
	private final Context context;

	static class Stats {
		private String properties;
		private String rooms;
		private String items;
		private String categories;
		private String backupSize;
	}

	private final NavigationView nav;
	public DrawerUpdateCountersTask(NavigationView nav) {
		this.nav = nav;
		this.context = nav.getContext();
	}
	@Override protected Stats doInBackground(@Nullable Void ignore) throws Exception {
		Stats stats = new Stats();
		Cursor cursor = App.db().stats();
		//noinspection TryFinallyCanBeTryWithResources
		try {
			long size = App.db().getFile().length();
			stats.properties = DatabaseTools.getOptionalString(cursor, "properties");
			stats.rooms = DatabaseTools.getOptionalString(cursor, "rooms");
			stats.items = DatabaseTools.getOptionalString(cursor, "items");
			stats.categories = DatabaseTools.getOptionalString(cursor, "categories");
			stats.backupSize = size != 0? Formatter.formatFileSize(context, size) : null;
		} finally {
			cursor.close();
		}
		return stats;
	}
	@Override protected void onResult(Stats result, Void ignore) {
		update(result);
	}

	@Override protected void onError(@NonNull Exception ex, Void ignore) {
		LOG.error("Cannot update counters on drawer", ex);
		update(new Stats());
	}

	private void update(Stats stats) {
		setMenuCounter(R.id.action_drawer_properties, stats.properties);
		setMenuCounter(R.id.action_drawer_rooms, stats.rooms);
		setMenuCounter(R.id.action_drawer_items, stats.items);
		setMenuCounter(R.id.action_drawer_categories, stats.categories);
		setMenuCounter(R.id.action_drawer_backup, stats.backupSize);
	}

	private void setMenuCounter(@IdRes int itemId, String count) {
		MenuItem item = nav.getMenu().findItem(itemId);
		TextView view = (TextView)MenuItemCompat.getActionView(item);
		if (view == null) {
			MenuItemCompat.setActionView(item, R.layout.inc_drawer_counter);
			view = (TextView)MenuItemCompat.getActionView(item);
		}
		view.setText(count);
	}
}
