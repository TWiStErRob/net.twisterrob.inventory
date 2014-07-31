package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.RoomListFragment.RoomEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertyTask;

public class PropertyViewActivity extends BaseListActivity implements RoomEvents {
	private RoomListFragment rooms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_view_activity);

		long currentPropertyID = getExtraPropertyID();
		if (currentPropertyID == Property.ID_ADD) {
			Toast.makeText(this, "Invalid property ID", Toast.LENGTH_LONG).show();
			finish();
		}

		rooms = RoomListFragment.newInstance(currentPropertyID);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.rooms, rooms);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		rooms.refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_edit:
				startActivity(PropertyEditActivity.edit(getExtraPropertyID()));
				return true;
			case R.id.action_property_delete:
				Dialogs.executeTask(this, new DeletePropertyTask(getExtraPropertyID(), new Dialogs.Callback() {
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
		startActivity(RoomEditActivity.add(getExtraPropertyID()));
	}

	public void roomSelected(long id, long rootItemID) {
		startActivity(ItemViewActivity.list(rootItemID));
	}

	public void roomActioned(long id) {
		startActivity(RoomEditActivity.edit(id));
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent list(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), PropertyViewActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}
}
