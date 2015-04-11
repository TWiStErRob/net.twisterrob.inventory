package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import android.app.ListActivity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.bumptech.glide.Glide;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.AndroidTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;

public class DeveloperActivity extends ListActivity {
	private static final Logger LOG = LoggerFactory.getLogger(DeveloperActivity.class);

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
						Glide.get(App.getAppContext()).clearMemory();
					}
				}),
				create("Restore DB", new Runnable() {
					@Override public void run() {
						final Context context = DeveloperActivity.this;
						AndroidTools
								.prompt(context, new PopupCallbacks<String>() {
									@Override public void finished(String value) {
										if (value == null) {
											return;
										}
										try {
											App.db().getHelper().restore(value);
											Toast.makeText(context, "Restored " + value, Toast.LENGTH_LONG).show();
										} catch (IOException ex) {
											LOG.error("Cannot restore {}", value, ex);
											Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
										}
									}
								})
								.setTitle("Restore DB")
								.setMessage("Please the absolute path of the .sqlite file to restore!")
								.show()
						;
					}
				}),
				create("Reset DB", new Runnable() {
					public void run() {
						DatabaseOpenHelper helper = App.db().getHelper();
						helper.close();
						helper.setTestMode(true);
						helper.getReadableDatabase();
						helper.close();
						helper.setTestMode(false);
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
