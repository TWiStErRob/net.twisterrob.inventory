package net.twisterrob.inventory.android.view;

import java.text.NumberFormat;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.*;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.*;

public class DrawerUpdateCountersTask extends SimpleSafeAsyncTask<Void, Void, DrawerUpdateCountersTask.Stats> {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerUpdateCountersTask.class);
	private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
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
	@Override protected Stats doInBackground(@Nullable Void ignore) {
		Stats stats = new Stats();
		Cursor cursor = App.db().stats();
		//noinspection TryFinallyCanBeTryWithResources
		try {
			long size = App.db().getFile().length();
			stats.properties = getFormattedInt(cursor, "properties");
			stats.rooms = getFormattedInt(cursor, "rooms");
			stats.items = getFormattedInt(cursor, "items");
			stats.categories = getFormattedInt(cursor, "categories");
			stats.backupSize = size != 0? Formatter.formatFileSize(context, size) : null;
		} finally {
			cursor.close();
		}
		return stats;
	}

	private String getFormattedInt(Cursor cursor, String column) {
		int value = DatabaseTools.getOptionalInt(cursor, column, -1);
		return value < 0? null : NUMBER.format(value);
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
		TextView view = (TextView)item.getActionView();
		if (view == null) {
			item.setActionView(R.layout.inc_drawer_counter);
			view = (TextView)item.getActionView();
		}
		view.setText(count);
	}
}
