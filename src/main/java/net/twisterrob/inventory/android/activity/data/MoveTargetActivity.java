package net.twisterrob.inventory.android.activity.data;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Loaders.LoadersCallbacks;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.*;

import static net.twisterrob.inventory.android.content.contract.ExtrasFactory.*;

public class MoveTargetActivity extends FragmentActivity implements OnBackStackChangedListener,
		PropertyListFragment.PropertiesEvents,
		RoomListFragment.RoomsEvents,
		ItemListFragment.ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(MoveTargetActivity.class);

	/**
	 * What to search for, use {@link #PROPERTY}, {@link #ROOM} and {@link #ITEM} as flags,
	 * one of the flags will be the result code in {@link android.app.Activity#onActivityResult}.
	 */
	private static final String EXTRA_WHAT = "what";
	private static final String EXTRA_START_TYPE = "start_type";
	private static final String EXTRA_START_ID = "start_id";
	private static final String EXTRA_NO_PROPERTIES = "no_properties";
	private static final String EXTRA_NO_ROOMS = "no_rooms";
	private static final String EXTRA_NO_ITEMS = "no_items";
	private static final int NOTHING = 0;
	/**
	 * To retrieve the selected property when received as a {@code resultCode} use
	 * {@code data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD)}.
	 */
	public static final int PROPERTY = 1 << 1;
	/**
	 * To retrieve the selected room when received as a {@code resultCode} use
	 * {@code data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD)}.
	 */
	public static final int ROOM = 1 << 2;
	/**
	 * To retrieve the selected item when received as a {@code resultCode} use
	 * {@code data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD)}.
	 */
	public static final int ITEM = 1 << 3;
	private static final int EVERYTHING = PROPERTY | ROOM | ITEM;

	private static final int REQUEST_ADD_PROPERTY = 1;
	private static final int REQUEST_ADD_ROOM = 2;
	private static final int REQUEST_ADD_ITEM = 3;

	private static final String ARG_TITLE = "move_title";
	private static final String ARG_FORBIDDEN = "move_forbidden";

	private View btnOk;
	private TextView labType;
	private TextView title;
	private final Handler handler = new Handler();

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

		PropertyListFragment fragment = PropertyListFragment.newInstance();
		updateFragment(fragment);
		updateUI(fragment);
		if (savedInstanceState == null) {
			switch (getArgStartType()) {
				case PROPERTY:
					propertySelected(getArgStartId(), true);
					break;
				case ROOM:
					roomSelected(getArgStartId(), true);
					break;
				case ITEM:
					itemSelected(getArgStartId(), true);
					break;
			}
		}
	}

	private void updateFragment(@NonNull BaseFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.activityRoot, fragment);
		if (getFragment() != null) { // prevent backing up to empty dialog
			ft.addToBackStack(null);
		}
		ft.commit();
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
		updateUI(getFragment());
	}

	private void updateUI(BaseFragment fragment) {
		String name = fragment.getArguments().getString(ARG_TITLE);
		if (name != null) {
			title.setText(getString(R.string.action_move_pick_title, toString(getType(fragment)), name));
		} else {
			title.setText(getString(R.string.action_move_pick_title_initial, toString(getArgWhat())));
		}
		CharSequence disabledMessage = buildDisabledMessage(fragment);
		if (disabledMessage != null) {
			btnOk.setEnabled(false);
			labType.setText(disabledMessage);
			labType.setVisibility(View.VISIBLE);
		} else {
			btnOk.setEnabled(true);
			labType.setVisibility(View.GONE);
			labType.setText(null);
		}
	}

	private CharSequence buildDisabledMessage(BaseFragment fragment) {
		int currentType = getType(fragment);
		int requestedType = getArgWhat();
		boolean allowed = (requestedType & currentType) != 0;
		boolean forbidden = fragment.getArguments().getBoolean(ARG_FORBIDDEN, false);

		String requested = toString(requestedType);
		if (forbidden) {
			return getString(R.string.action_move_pick_forbidden, requested);
		}
		if (!allowed) {
			return getString(R.string.action_move_pick_requested, requested);
		}
		return null;
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

	private void startFragment(final BaseFragment fragment) {
		// FragmentTransaction.commit: Can not perform this action inside of onLoadFinished
		// so must do it on the UI thread, but later!
		handler.post(new Runnable() {
			@Override public void run() {
				updateFragment(fragment);
			}
		});
	}

	@Override public void propertySelected(long propertyID) {
		propertySelected(propertyID, false);
	}
	private void propertySelected(long propertyID, boolean startMode) {
		Bundle args = bundleFromProperty(propertyID);
		getSupportLoaderManager().destroyLoader(Loaders.SingleProperty.ordinal());
		getSupportLoaderManager().initLoader(Loaders.SingleProperty.ordinal(), args, new LoadSingleRow(this) {
			@Override protected void process(Cursor cursor) {
				PropertyDTO property = PropertyDTO.fromCursor(cursor);
				BaseFragment fragment = RoomListFragment.newInstance(property.id);
				fragment.getArguments().putString(ARG_TITLE, property.name);
				if (isForbidden(EXTRA_NO_PROPERTIES, property.id)) {
					fragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
				}
				startFragment(fragment);
			}
		});
	}

	@Override public void roomSelected(long roomID) {
		roomSelected(roomID, false);
	}
	private void roomSelected(long roomID, final boolean startMode) {
		Bundle args = bundleFromRoom(roomID);
		getSupportLoaderManager().destroyLoader(Loaders.SingleRoom.ordinal());
		getSupportLoaderManager().initLoader(Loaders.SingleRoom.ordinal(), args, new LoadSingleRow(this) {
			@Override protected void process(Cursor cursor) {
				RoomDTO room = RoomDTO.fromCursor(cursor);
				BaseFragment roomFragment = ItemListFragment.newRoomInstance(room.id);
				roomFragment.getArguments().putString(ARG_TITLE, room.name);
				if (isForbidden(EXTRA_NO_PROPERTIES, room.propertyID) || isForbidden(EXTRA_NO_ROOMS, room.id)) {
					roomFragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
				}
				if (startMode) {
					BaseFragment propertyFragment = RoomListFragment.newInstance(room.propertyID);
					propertyFragment.getArguments().putString(ARG_TITLE, room.propertyName);
					if (isForbidden(EXTRA_NO_PROPERTIES, room.propertyID)) {
						propertyFragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
					}
					startFragment(propertyFragment);
				}
				startFragment(roomFragment);
			}
		});
	}

	@Override public void itemSelected(long itemID) {
		itemSelected(itemID, false);
	}
	private void itemSelected(final long itemID, final boolean startMode) {
		Bundle args = bundleFromItem(itemID);
		getSupportLoaderManager().destroyLoader(Loaders.ItemParents.ordinal());
		getSupportLoaderManager().initLoader(Loaders.ItemParents.ordinal(), args, new LoadersCallbacks(this) {
			@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				boolean forbidden = false;
				while (data.moveToNext()) {
					long id = data.getLong(data.getColumnIndexOrThrow(ParentColumns.ID));
					Type type = Type.from(data.getString(data.getColumnIndexOrThrow(ParentColumns.TYPE)));
					forbidden = forbidden || isAnyForbidden(id, type);
					LOG.trace("itemSelected({}, {}): {}#{} -> {}", itemID, startMode, type, id, forbidden);
					if (type.isMain() && (startMode || data.isLast())) {
						BaseFragment fragment = createFragment(type, id);
						String name = data.getString(data.getColumnIndexOrThrow(ParentColumns.NAME));
						fragment.getArguments().putString(ARG_TITLE, name);
						fragment.getArguments().putBoolean(ARG_FORBIDDEN, forbidden);
						LOG.trace("created: {}: {}", fragment, fragment.getArguments());
						startFragment(fragment);
					}
				}
			}

			private BaseFragment createFragment(Type type, long id) {
				switch (type) {
					case Property:
						return RoomListFragment.newInstance(id);
					case Room:
						return ItemListFragment.newRoomInstance(id);
					case Item:
						return ItemListFragment.newInstance(id);
				}
				throw new IllegalArgumentException("Cannot create fragment for " + type + " with id=" + id);
			}

			private boolean isAnyForbidden(long id, Type type) {
				return type == Type.Item && isForbidden(EXTRA_NO_ITEMS, id)
						|| type == Type.Room && isForbidden(EXTRA_NO_ROOMS, id)
						|| type == Type.Property && isForbidden(EXTRA_NO_PROPERTIES, id);
			}
			@Override public void onLoaderReset(Loader<Cursor> loader) {
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

	private int getArgStartType() {
		return getIntent().getIntExtra(EXTRA_START_TYPE, NOTHING);
	}
	private long getArgStartId() {
		return getIntent().getLongExtra(EXTRA_START_ID, CommonColumns.ID_ADD);
	}
	private int getArgWhat() {
		return getIntent().getIntExtra(EXTRA_WHAT, NOTHING) & EVERYTHING;
	}

	private boolean isForbidden(String extra, long id) {
		long[] ids = getIntent().getLongArrayExtra(extra);
		return ids != null && 0 <= Arrays.binarySearch(ids, id);
	}

	public static Builder pick() {
		return new Builder();
	}

	public static final class Builder {
		private final Intent intent = new Intent(App.getAppContext(), MoveTargetActivity.class);
		private int what;
		private final Set<Long> propertyIDs = new TreeSet<>();
		private final Set<Long> roomIDs = new TreeSet<>();
		private final Set<Long> itemIDs = new TreeSet<>();
		public Builder() {
			allowNothing();
		}

		public Builder allowEverything() {
			what = EVERYTHING;
			return this;
		}
		public Builder allowNothing() {
			what = NOTHING;
			return this;
		}
		public Builder allowProperties() {
			what |= PROPERTY;
			return this;
		}
		public Builder allowRooms() {
			what |= ROOM;
			return this;
		}
		public Builder allowItems() {
			what |= ITEM;
			return this;
		}
		public Builder disallowProperties() {
			what &= ~PROPERTY;
			return this;
		}
		public Builder disallowRooms() {
			what &= ~ROOM;
			return this;
		}
		public Builder disallowItems() {
			what &= ~ITEM;
			return this;
		}
		public Builder forbidProperties(long... propertyIDs) {
			for (long id : propertyIDs) {
				this.propertyIDs.add(id);
			}
			return this;
		}
		public Builder forbidRooms(long... roomIDs) {
			for (long id : roomIDs) {
				this.roomIDs.add(id);
			}
			return this;
		}
		public Builder forbidItems(long... itemIDs) {
			for (long id : itemIDs) {
				this.itemIDs.add(id);
			}
			return this;
		}
		public Builder startFromPropertyList() {
			intent.removeExtra(EXTRA_START_TYPE);
			intent.removeExtra(EXTRA_START_ID);
			return this;
		}
		public Builder startFromProperty(long propertyID) {
			if (propertyID != Property.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, PROPERTY);
				intent.putExtra(EXTRA_START_ID, propertyID);
			}
			return this;
		}
		public Builder startFromRoom(long roomID) {
			if (roomID != Room.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, ROOM);
				intent.putExtra(EXTRA_START_ID, roomID);
			}
			return this;
		}
		public Builder startFromItem(long itemID) {
			if (itemID != Item.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, ITEM);
				intent.putExtra(EXTRA_START_ID, itemID);
			}
			return this;
		}

		public Intent build() {
			intent.putExtra(EXTRA_WHAT, what);
			if (!propertyIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_PROPERTIES, toArr(propertyIDs));
			}
			if (!roomIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_ROOMS, toArr(roomIDs));
			}
			if (!itemIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_ITEMS, toArr(itemIDs));
			}
			return intent;
		}

		private static long[] toArr(Collection<Long> ids) {
			long[] arr = new long[ids.size()];
			Iterator<Long> it = ids.iterator();
			for (int i = 0; i < arr.length; i++) {
				arr[i] = it.next();
			}
			return arr;
		}
	}
}
