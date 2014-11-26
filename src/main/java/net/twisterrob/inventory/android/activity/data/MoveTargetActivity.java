package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.app.FragmentManager.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.*;

public class MoveTargetActivity extends FragmentActivity implements OnBackStackChangedListener,
		PropertyListFragment.PropertiesEvents,
		RoomListFragment.RoomsEvents,
		ItemListFragment.ItemsEvents {

	private static final int ADD_PROPERTY = 1;
	private static final int ADD_ROOM = 2;
	private static final int ADD_ITEM = 3;

	public static final int RESULT_PROPERTY = RESULT_FIRST_USER + 1;
	public static final int RESULT_ROOM = RESULT_FIRST_USER + 2;
	public static final int RESULT_ITEM = RESULT_FIRST_USER + 3;

	private Handler handler = new Handler();
	private View btnOk;
	private TextView title;

	private BaseFragment getFragment() {
		return (BaseFragment)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move);
		title = (TextView)findViewById(R.id.selection);
		ImageButton imageButton = (ImageButton)findViewById(R.id.up);
		imageButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				getSupportFragmentManager().popBackStack();
			}
		});
		findViewById(android.R.id.closeButton).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setResult(null);
				finish();
			}
		});
		btnOk = findViewById(android.R.id.button1);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setResult(getFragment());
				finish();
			}
		});
		getSupportFragmentManager().addOnBackStackChangedListener(this);

		updateFragment(PropertyListFragment.newInstance(), null);
		onBackStackChanged();
	}

	private void updateFragment(@NonNull BaseFragment fragment, String title) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.activityRoot, fragment);
		if (getFragment() != null) {
			ft.addToBackStack(title); // prevent backing up to empty dialog
		}
		ft.commit();
	}

	private void setResult(Fragment fragment) {
		setResult(RESULT_CANCELED);
		if (fragment instanceof RoomListFragment) {
			long propertyID = fragment.getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			setResult(RESULT_PROPERTY, ExtrasFactory.intentFromProperty(propertyID));
		} else if (fragment instanceof ItemListFragment) {
			long roomID = fragment.getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
			long itemID = fragment.getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
			if (itemID != Item.ID_ADD) {
				setResult(RESULT_ITEM, ExtrasFactory.intentFromItem(itemID));
			} else if (roomID != Room.ID_ADD) {
				setResult(RESULT_ROOM, ExtrasFactory.intentFromRoom(roomID));
			}
		}
	}

	@Override public void onBackStackChanged() {
		FragmentManager manager = getSupportFragmentManager();
		int count = manager.getBackStackEntryCount();
		if (count > 0) {
			BackStackEntry at = manager.getBackStackEntryAt(count - 1);
			setTitle(at.getName());
		} else {
			setTitle(null);
		}
	}

	private void setTitle(String name) {
		if (name != null) {
			btnOk.setEnabled(true);
			title.setText(getString(R.string.move_title, name));
		} else {
			btnOk.setEnabled(false);
			title.setText(R.string.move_title_question);
		}
	}

	@Override public void newProperty() {
		startActivityForResult(PropertyEditActivity.add(), ADD_PROPERTY);
	}
	@Override public void newRoom(long propertyID) {
		startActivityForResult(RoomEditActivity.add(propertyID), ADD_ROOM);
	}
	@Override public void newItem(long parentID) {
		startActivityForResult(ItemEditActivity.add(parentID), ADD_ITEM);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			getFragment().refresh();
			switch (requestCode) {
				case ADD_PROPERTY:
					propertySelected(data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD));
					break;
				case ADD_ROOM:
					roomSelected(data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD));
					break;
				case ADD_ITEM:
					itemSelected(data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD));
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void propertySelected(long propertyID) {
		load(Loaders.SingleProperty, Extras.PROPERTY_ID, propertyID, RoomListFragment.newInstance(propertyID));
	}
	@Override public void roomSelected(long roomID) {
		load(Loaders.SingleRoom, Extras.ROOM_ID, roomID, ItemListFragment.newRoomInstance(roomID));
	}
	@Override public void itemSelected(long itemID) {
		load(Loaders.SingleItem, Extras.ITEM_ID, itemID, ItemListFragment.newInstance(itemID));
	}
	private void load(final Loaders singleLoader, String extraID, long id, final BaseFragment fragment) {
		Bundle args = new Bundle();
		args.putLong(extraID, id);
		getSupportLoaderManager().initLoader(singleLoader.ordinal(), args, new LoadSingleRow(this) {
			@Override protected void process(Cursor cursor) {
				final DTO data = RoomDTO.fromCursor(cursor);
				getSupportLoaderManager().destroyLoader(singleLoader.ordinal());
				// FragmentTransaction.commit: Can not perform this action inside of onLoadFinished
				// so must do it on the UI thread, but later!
				handler.post(new Runnable() {
					@Override public void run() {
						updateFragment(fragment, data.name);
					}
				});
			}
		});
	}

	@Override public void propertyActioned(long propertyID) {
		propertySelected(propertyID);
	}
	@Override public void roomActioned(long roomID) {
		roomSelected(roomID);
	}
	@Override public void itemActioned(long itemID) {
		itemSelected(itemID);
	}

	public static Intent pick() {
		Intent intent = new Intent(App.getAppContext(), MoveTargetActivity.class);
		return intent;
	}
}
