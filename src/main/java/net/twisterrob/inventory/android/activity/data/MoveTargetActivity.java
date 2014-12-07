package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.app.FragmentManager.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.*;

import static net.twisterrob.inventory.android.content.contract.ExtrasFactory.*;

public class MoveTargetActivity extends FragmentActivity implements OnBackStackChangedListener,
		PropertyListFragment.PropertiesEvents,
		RoomListFragment.RoomsEvents,
		ItemListFragment.ItemsEvents {
	/**
	 * What to search for, use {@link #PROPERTY}, {@link #ROOM} and {@link #ITEM} as flags,
	 * one of the flags will be the result code in {@link android.app.Activity#onActivityResult}.
	 */
	public static final String EXTRA_WHAT = "what";
	public static final int NOTHING = 0;
	/**
	 * Allow only Rooms to be selected. When received as a {@code resultCode}
	 * use {@code data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD)} to retrieve the selection.
	 */
	public static final int PROPERTY = 1 << 1;
	/**
	 * Allow only Rooms to be selected. When received as a {@code resultCode}
	 * use {@code data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD)} to retrieve the selection.
	 */
	public static final int ROOM = 1 << 2;
	/**
	 * Allow only Rooms to be selected. When received as a {@code resultCode}
	 * use {@code data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD)} to retrieve the selection.
	 */
	public static final int ITEM = 1 << 3;
	public static final int EVERYTHING = PROPERTY | ROOM | ITEM;

	private static final int REQUEST_ADD_PROPERTY = 1;
	private static final int REQUEST_ADD_ROOM = 2;
	private static final int REQUEST_ADD_ITEM = 3;

	private View btnOk;
	private TextView labType;
	private TextView title;

	private BaseFragment getFragment() {
		return (BaseFragment)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move);
		title = (TextView)findViewById(R.id.selection);
		labType = (TextView)findViewById(R.id.type);
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
	}

	private void updateFragment(@NonNull BaseFragment fragment, String title) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.activityRoot, fragment);
		if (getFragment() != null) {
			ft.addToBackStack(title); // prevent backing up to empty dialog
		}
		ft.commit();
		updateUI(fragment, title);
	}

	private void setResult(Fragment fragment) {
		setResult(RESULT_CANCELED);
		if (fragment instanceof RoomListFragment) {
			long propertyID = fragment.getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			setResult(PROPERTY, ExtrasFactory.intentFromProperty(propertyID));
		} else if (fragment instanceof ItemListFragment) {
			long roomID = fragment.getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
			long itemID = fragment.getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
			if (roomID != Room.ID_ADD) {
				setResult(ROOM, ExtrasFactory.intentFromRoom(roomID));
			} else if (itemID != Item.ID_ADD) {
				setResult(ITEM, ExtrasFactory.intentFromItem(itemID));
			}
		}
	}

	private int getType(Fragment fragment) {
		if (fragment instanceof RoomListFragment) {
			return PROPERTY;
		} else if (fragment instanceof ItemListFragment) {
			long roomID = fragment.getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
			if (roomID != Room.ID_ADD) {
				return ROOM;
			} else {
				return ITEM;
			}
		} else {
			return NOTHING;
		}
	}

	@Override public void onBackStackChanged() {
		FragmentManager manager = getSupportFragmentManager();
		int count = manager.getBackStackEntryCount();
		if (count > 0) {
			BackStackEntry at = manager.getBackStackEntryAt(count - 1);
			updateUI(getFragment(), at.getName());
		} else {
			updateUI(getFragment(), null);
		}
	}

	private void updateUI(BaseFragment fragment, String name) {
		int currentType = getType(fragment);
		int requestedType = getArgWhat();
		boolean allowed = (requestedType & currentType) != 0;

		if (name != null) {
			title.setText(getString(R.string.move_title, toString(currentType), name));
		} else {
			title.setText(R.string.move_title_question);
		}
		if (allowed) {
			labType.setVisibility(View.GONE);
			labType.setText(null);
		} else {
			labType.setVisibility(View.VISIBLE);
			String typeString = getString(R.string.move_current_type, toString(requestedType));
			labType.setText(typeString);
		}
		btnOk.setEnabled(allowed);
	}

	private String toString(int type) {
		int stringResource;
		switch (type) {
			case PROPERTY:
				stringResource = R.string.property_one;
				break;
			case ROOM:
				stringResource = R.string.room_one;
				break;
			case ITEM:
				stringResource = R.string.item_one;
				break;
			case PROPERTY | ROOM:
				return toString(PROPERTY) + "/" + toString(ROOM);
			case PROPERTY | ITEM:
				return toString(PROPERTY) + "/" + toString(ITEM);
			case ROOM | ITEM:
				return toString(ROOM) + "/" + toString(ITEM);
			case EVERYTHING:
				return toString(PROPERTY) + "/" + toString(ROOM) + "/" + toString(ITEM);
			default:
				return "???";
		}
		return getString(stringResource);
	}

	@Override public void newProperty() {
		startActivityForResult(PropertyEditActivity.add(), REQUEST_ADD_PROPERTY);
	}
	@Override public void newRoom(long propertyID) {
		startActivityForResult(RoomEditActivity.add(propertyID), REQUEST_ADD_ROOM);
	}
	@Override public void newItem(long parentID) {
		startActivityForResult(ItemEditActivity.add(parentID), REQUEST_ADD_ITEM);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			getFragment().refresh();
			switch (requestCode) {
				case REQUEST_ADD_PROPERTY:
					propertySelected(data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD));
					break;
				case REQUEST_ADD_ROOM:
					roomSelected(data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD));
					break;
				case REQUEST_ADD_ITEM:
					itemSelected(data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD));
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void propertySelected(long propertyID) {
		load(Loaders.SingleProperty, bundleFromProperty(propertyID), RoomListFragment.newInstance(propertyID));
	}
	@Override public void roomSelected(long roomID) {
		load(Loaders.SingleRoom, bundleFromRoom(roomID), ItemListFragment.newRoomInstance(roomID));
	}
	@Override public void itemSelected(long itemID) {
		load(Loaders.SingleItem, bundleFromItem(itemID), ItemListFragment.newInstance(itemID));
	}
	private void load(final Loaders singleLoader, Bundle args, final BaseFragment fragment) {
		getSupportLoaderManager().initLoader(singleLoader.ordinal(), args, new LoadSingleRow(this) {
			@Override protected void process(Cursor cursor) {
				final String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
				getSupportLoaderManager().destroyLoader(singleLoader.ordinal());
				// FragmentTransaction.commit: Can not perform this action inside of onLoadFinished
				// so must do it on the UI thread, but later!
				runOnUiThread(new Runnable() {
					@Override public void run() {
						updateFragment(fragment, name);
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

	private int getArgWhat() {
		return getIntent().getIntExtra(EXTRA_WHAT, NOTHING) & EVERYTHING;
	}

	public static Intent pick() {
		return pick(EVERYTHING);
	}
	public static Intent pick(int what) {
		Intent intent = new Intent(App.getAppContext(), MoveTargetActivity.class);
		intent.putExtra(EXTRA_WHAT, what);
		return intent;
	}
}
