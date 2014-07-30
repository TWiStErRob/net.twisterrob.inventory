package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.RoomsFragment.RoomEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertyTask;

public class RoomsActivity extends BaseListActivity implements RoomEvents {
	private long currentPropertyID;
	private RoomsFragment rooms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.rooms);

		currentPropertyID = getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
		if (currentPropertyID == Property.ID_ADD) {
			Toast.makeText(this, "Invalid property ID", Toast.LENGTH_LONG).show();
			finish();
		}
		rooms = getFragment(R.id.rooms);
	}

	@Override
	protected void onStart() {
		super.onStart();
		rooms.listForProperty(currentPropertyID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		rooms.refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.room, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_edit:
				startActivity(PropertyEditActivity.edit(currentPropertyID));
				return true;
			case R.id.action_property_delete:
				Dialogs.executeTask(this, new DeletePropertyTask(currentPropertyID, new Dialogs.Callback() {
					public void success() {
						finish();
					}
					public void failed() {
						String message = "This property still has some rooms";
						Toast.makeText(App.getAppContext(), message, Toast.LENGTH_LONG).show();
					}
				}));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void newRoom() {
		startActivity(RoomEditActivity.add());
	}

	public void roomSelected(long id, long rootItemID) {
		ItemsFragment list = getFragment(R.id.items);
		if (list != null && list.isInLayout()) {
			list.list(rootItemID);
		} else {
			startActivity(ItemsActivity.list(rootItemID));
		}
	}

	public void roomActioned(long id) {
		RoomEditFragment editor = getFragment(R.id.room);
		if (editor != null && editor.isInLayout()) {
			editor.load(id);
		} else {
			startActivity(RoomEditActivity.edit(id));
		}
	}

	public static Intent list(long propertyId) {
		Intent intent = new Intent(App.getAppContext(), RoomsActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyId);
		return intent;
	}
}
