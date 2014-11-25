package net.twisterrob.inventory.android.activity.dev;

import java.io.File;
import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Database;

public class DeveloperActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> entries = Arrays.asList( //
				create(getString(R.string.app_name), new Runnable() {
					public void run() {
						Intent intent = new Intent(App.getAppContext(), MainActivity.class);
						startActivity(intent);
					}
				}),
				create("TEST", new Runnable() {
					public void run() {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("content://net.twisterrob.inventory/item/100008"));
						startActivity(intent);
					}
				}),
				create(getString(R.string.property_details), new Runnable() {
					public void run() {
						startActivity(PropertyViewActivity.show(1));
					}
				}),
				create(getString(R.string.property_edit), new Runnable() {
					public void run() {
						startActivity(PropertyEditActivity.edit(1));
					}
				}),
				create(getString(R.string.room_details), new Runnable() {
					public void run() {
						startActivity(RoomViewActivity.show(4));
					}
				}),
				create(getString(R.string.room_edit), new Runnable() {
					public void run() {
						startActivity(RoomEditActivity.edit(4));
					}
				}),
				create(getString(R.string.item_details), new Runnable() {
					public void run() {
						startActivity(ItemViewActivity.show(100008));
					}
				}),
				create(getString(R.string.item_edit), new Runnable() {
					public void run() {
						startActivity(ItemEditActivity.edit(100008));
					}
				}),
				create(getString(R.string.item_details) + " root", new Runnable() {
					public void run() {
						startActivity(ItemViewActivity.show(5));
					}
				}),
				create(getString(R.string.item_edit) + " root", new Runnable() {
					public void run() {
						startActivity(ItemEditActivity.edit(5));
					}
				}),
				create("Clear Image cache", new Runnable() {
					public void run() {
						App.pic().clearCaches();
					}
				}),
				create("Reset DB", new Runnable() {
					public void run() {
						Database db = new Database(App.getAppContext());
						db.getHelper().setDevMode(true);
						db.getReadableDatabase().close();
					}
				}),
				create("Drive Chooser", new Runnable() {
					public void run() {
						Intent intent = new Intent(App.getAppContext(), PickDriveFileActivity.class);
						startActivity(intent);
					}
				}),
				create("Drive Test", new Runnable() {
					public void run() {
						Intent intent = new Intent(App.getAppContext(), DeveloperDriveActivity.class);
						startActivity(intent);
					}
				}),
				create(getString(R.string.action_picture_take), new Runnable() {
					public void run() {
						File target = new File(getCacheDir(), "developer_image.jpg");
						startActivity(CaptureImage.saveTo(App.getAppContext(), target));
					}
				})
		);
		SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), entries,
				android.R.layout.simple_list_item_1, new String[] {"title"}, new int[] {android.R.id.text1});
		setListAdapter(simpleAdapter);
	}

	private static Map<String, Object> create(String title, Runnable action) {
		Map<String, Object> entry = new HashMap<>();
		entry.put("title", title);
		entry.put("action", action);
		return entry;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		Map<String, ?> x = (Map<String, ?>)l.getItemAtPosition(position);
		Runnable action = (Runnable)x.get("action");
		action.run();
	}

	public static Intent show() {
		Intent intent = new Intent(App.getAppContext(), DeveloperActivity.class);
		return intent;
	}
}
