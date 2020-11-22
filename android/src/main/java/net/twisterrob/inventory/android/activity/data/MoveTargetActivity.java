package net.twisterrob.inventory.android.activity.data;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import net.twisterrob.android.utils.tools.ViewTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.InventoryLoader.LoadersCallbacksAdapter;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.*;

public class MoveTargetActivity extends BaseActivity implements OnBackStackChangedListener,
		PropertyListFragment.PropertiesEvents,
		RoomListFragment.RoomsEvents,
		ItemListFragment.ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(MoveTargetActivity.class);

	/**
	 * What to search for, use {@link #PROPERTY}, {@link #ROOM} and {@link #ITEM} as {@linkplain BelongingTarget flags},
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

	@IntDef(flag = true, value = {NOTHING, PROPERTY, ROOM, ITEM})
	private @interface BelongingTarget {
	}

	private static final int REQUEST_ADD_PROPERTY = 1;
	private static final int REQUEST_ADD_ROOM = 2;
	private static final int REQUEST_ADD_ITEM = 3;

	private static final String ARG_TITLE = "move_title";
	private static final String ARG_FORBIDDEN = "move_forbidden";

	private TextView title;
	private TextView labType;
	private View btnOk;
	private final Handler handler = new Handler();

	private BaseFragment<?> getFragment() {
		return getFragment(R.id.activityRoot);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fixArray(EXTRA_NO_PROPERTIES);
		fixArray(EXTRA_NO_ROOMS);
		fixArray(EXTRA_NO_ITEMS);

		setContentView(R.layout.activity_move);
		title = (TextView)findViewById(R.id.selection);
		labType = (TextView)findViewById(R.id.type);
		findViewById(android.R.id.button2).setOnClickListener(new OnClickListener() {
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

		// CONSIDER this extends SingleFragmentActivity?
		if (savedInstanceState == null) {
			PropertyListFragment fragment = PropertyListFragment.newInstance();
			updateFragment(fragment);
			updateUI(fragment);
			switch (getArgStartType()) {
				case NOTHING:
					// show property list (instantiated just above)
					break;
				case PROPERTY:
					propertySelected(getArgStartId(), true);
					break;
				case ROOM:
					roomSelected(getArgStartId(), true);
					break;
				case ITEM:
					itemSelected(getArgStartId(), true);
					break;
				default:
					throw new IllegalArgumentException("Cannot start browsing at " + toString(getArgStartType()));
			}
		} else {
			updateUI(getFragment());
		}
	}

	private void fixArray(String properties) {
		long[] forbidden = getIntent().getLongArrayExtra(properties);
		if (forbidden != null) {
			Arrays.sort(forbidden);
		}
	}

	private void updateFragment(@NonNull BaseFragment<?> fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		LOG.trace("Using fragment {}", fragment);
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
			setResult(PROPERTY, Intents.intentFromProperty(propertyID));
		} else if (fragment instanceof ItemListFragment) {
			long roomID = fragment.getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
			long itemID = fragment.getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
			if (roomID != Room.ID_ADD) {
				setResult(ROOM, Intents.intentFromRoom(roomID));
			} else if (itemID != Item.ID_ADD) {
				setResult(ITEM, Intents.intentFromItem(itemID));
			}
		}
	}

	private @BelongingTarget int getType(Fragment fragment) {
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

	private void updateUI(BaseFragment<?> fragment) {
		String name = fragment.getArguments().getString(ARG_TITLE);
		if (name != null) {
			setActionBarTitle(getString(R.string.action_move_pick_title));
			setActionBarSubtitle(toString(getType(fragment)));
		} else {
			setActionBarTitle(getString(R.string.action_move));
			setActionBarSubtitle(null);
			name = getString(R.string.property_list);
		}
		title.setText(name);
		CharSequence disabledMessage = buildDisabledMessage(fragment);
		if (disabledMessage != null) {
			btnOk.setEnabled(false);
			labType.setText(disabledMessage);
		} else {
			btnOk.setEnabled(true);
			labType.setText(null);
		}
		ViewTools.displayedIfHasText(labType);
		ViewTools.displayedIfHasText(title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(0 < getSupportFragmentManager().getBackStackEntryCount());
	}

	@SuppressWarnings("TooBroadScope")
	private CharSequence buildDisabledMessage(BaseFragment<?> fragment) {
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

	private String toString(@BelongingTarget int type) {
		int typeResource;
		switch (type) {
			case PROPERTY:
				typeResource = R.plurals.property;
				break;
			case ROOM:
				typeResource = R.plurals.room;
				break;
			case ITEM:
				typeResource = R.plurals.item;
				break;
			case PROPERTY | ROOM:
				return toString(PROPERTY) + "/" + toString(ROOM);
			case PROPERTY | ITEM:
				return toString(PROPERTY) + "/" + toString(ITEM);
			case ROOM | ITEM:
				return toString(ROOM) + "/" + toString(ITEM);
			case PROPERTY | ROOM | ITEM:
				return toString(PROPERTY) + "/" + toString(ROOM) + "/" + toString(ITEM);
			case NOTHING:
			default:
				return BelongingTarget.class.getSimpleName() + "::" + Integer.toBinaryString(type);
		}
		return getResources().getQuantityString(typeResource, 1);
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

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		ViewTools.visibleIf(menu, R.id.action_room_list, false);
		return result;
	}

	@Override public boolean onSupportNavigateUp() {
		FragmentManager fm = getSupportFragmentManager();
		if (0 < fm.getBackStackEntryCount()) {
			fm.popBackStack();
			return true;
		} // else it shouldn't be visible so this method is not called
		return super.onSupportNavigateUp();
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

	private void startFragment(final BaseFragment<?> fragment) {
		if (fragment instanceof BaseGalleryFragment) {
			fragment.getArguments().putBoolean(BaseGalleryFragment.KEY_ENABLE_SELECTION, false);
		}
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
	private void propertySelected(long propertyID, final boolean startMode) {
		Bundle args = Intents.bundleFromProperty(propertyID);
		Loaders loader = Loaders.SingleProperty;
		getSupportLoaderManager().destroyLoader(loader.id());
		getSupportLoaderManager().initLoader(loader.id(), args, loader.createCallbacks(this, new LoadSingleRow() {
			@Override protected void process(@NonNull Cursor cursor) {
				PropertyDTO property = PropertyDTO.fromCursor(cursor);
				BaseFragment<?> fragment = RoomListFragment.newInstance(property.id);
				fragment.getArguments().putString(ARG_TITLE, property.name);
				if (isForbidden(EXTRA_NO_PROPERTIES, property.id)) {
					fragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
				}
				if (startMode) {
					startFragment(fragment);
				} else {
					startFragment(fragment);
				}
			}
		}));
	}

	@Override public void roomSelected(long roomID) {
		roomSelected(roomID, false);
	}
	private void roomSelected(long roomID, final boolean startMode) {
		Bundle args = Intents.bundleFromRoom(roomID);
		Loaders loader = Loaders.SingleRoom;
		getSupportLoaderManager().destroyLoader(loader.id());
		getSupportLoaderManager().initLoader(loader.id(), args, loader.createCallbacks(this, new LoadSingleRow() {
			@Override protected void process(@NonNull Cursor cursor) {
				RoomDTO room = RoomDTO.fromCursor(cursor);
				BaseFragment<?> roomFragment = ItemListFragment.newRoomInstance(room.id);
				roomFragment.getArguments().putString(ARG_TITLE, room.name);
				if (isForbidden(EXTRA_NO_PROPERTIES, room.propertyID) || isForbidden(EXTRA_NO_ROOMS, room.id)) {
					roomFragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
				}
				if (startMode) {
					BaseFragment<?> propertyFragment = RoomListFragment.newInstance(room.propertyID);
					propertyFragment.getArguments().putString(ARG_TITLE, room.propertyName);
					if (isForbidden(EXTRA_NO_PROPERTIES, room.propertyID)) {
						propertyFragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
					}
					startFragment(propertyFragment);
				}
				startFragment(roomFragment);
			}
		}));
	}

	@Override public void itemSelected(long itemID) {
		itemSelected(itemID, false);
	}
	private void itemSelected(final long itemID, final boolean startMode) {
		Bundle args = Intents.bundleFromItem(itemID);
		Loaders loader = Loaders.ItemParents;
		LoaderManager loadManager = getSupportLoaderManager();
		loadManager.destroyLoader(loader.id());
		loadManager.initLoader(loader.id(), args, loader.createCallbacks(this, new LoadersCallbacksAdapter() {
			@Override public void postOnLoadFinished(Loader<Cursor> loader,
					Cursor data) {
				boolean forbidden = false;
				while (data.moveToNext()) {
					long id = data.getLong(data.getColumnIndexOrThrow(ParentColumns.ID));
					Type type = Type.from(data.getString(data.getColumnIndexOrThrow(ParentColumns.PARENT_TYPE)));
					forbidden = forbidden || isAnyForbidden(id, type);
					if (type.isMain() && (startMode || data.isLast())) {
						BaseFragment<?> fragment = createFragment(type, id);
						String name = data.getString(data.getColumnIndexOrThrow(ParentColumns.NAME));
						fragment.getArguments().putString(ARG_TITLE, name);
						fragment.getArguments().putBoolean(ARG_FORBIDDEN, forbidden);
						startFragment(fragment);
					}
				}
			}

			private BaseFragment<?> createFragment(Type type, long id) {
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
		}));
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

	@SuppressWarnings("ResourceType")
	private @BelongingTarget int getArgStartType() {
		return getIntent().getIntExtra(EXTRA_START_TYPE, NOTHING) & EVERYTHING;
	}
	private long getArgStartId() {
		return getIntent().getLongExtra(EXTRA_START_ID, CommonColumns.ID_ADD);
	}
	@SuppressWarnings("ResourceType")
	private @BelongingTarget int getArgWhat() {
		return getIntent().getIntExtra(EXTRA_WHAT, NOTHING) & EVERYTHING;
	}

	private boolean isForbidden(String extra, long id) {
		long[] ids = getIntent().getLongArrayExtra(extra);
		return ids != null && 0 <= Arrays.binarySearch(ids, id);
	}

	public static Builder pick() {
		return new Builder();
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public static final class Builder {
		// cannot automatically forbid starting points, because siblings would be disabled

		private final Intent intent = new Intent(App.getAppContext(), MoveTargetActivity.class);
		private @BelongingTarget int what;
		@SuppressWarnings("TypeMayBeWeakened")
		private final Set<Long> forbiddenPropertyIDs = new TreeSet<>();
		@SuppressWarnings("TypeMayBeWeakened")
		private final Set<Long> forbiddenRoomIDs = new TreeSet<>();
		@SuppressWarnings("TypeMayBeWeakened")
		private final Set<Long> forbiddenItemIDs = new TreeSet<>();
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
				forbiddenPropertyIDs.add(id);
			}
			return this;
		}
		public Builder forbidRooms(long... roomIDs) {
			for (long id : roomIDs) {
				forbiddenRoomIDs.add(id);
			}
			return this;
		}
		public Builder forbidItems(long... itemIDs) {
			for (long id : itemIDs) {
				forbiddenItemIDs.add(id);
			}
			return this;
		}
		public Builder resetForbidProperties() {
			forbiddenPropertyIDs.clear();
			return this;
		}
		public Builder resetForbidRooms() {
			forbiddenRoomIDs.clear();
			return this;
		}
		public Builder resetForbidItems() {
			forbiddenItemIDs.clear();
			return this;
		}
		public Builder startFromPropertyList() {
			checkAlreadyStarted();
			intent.removeExtra(EXTRA_START_TYPE);
			intent.removeExtra(EXTRA_START_ID);
			return this;
		}
		public Builder startFromProperty(long propertyID) {
			checkAlreadyStarted();
			if (propertyID != Property.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, PROPERTY);
				intent.putExtra(EXTRA_START_ID, propertyID);
			} else {
				throw new IllegalArgumentException("Cannot start from a new property.");
			}
			return this;
		}
		public Builder startFromRoom(long roomID) {
			checkAlreadyStarted();
			if (roomID != Room.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, ROOM);
				intent.putExtra(EXTRA_START_ID, roomID);
			} else {
				throw new IllegalArgumentException("Cannot start from a new room.");
			}
			return this;
		}
		public Builder startFromItem(long itemID) {
			checkAlreadyStarted();
			if (itemID != Item.ID_ADD) {
				intent.putExtra(EXTRA_START_TYPE, ITEM);
				intent.putExtra(EXTRA_START_ID, itemID);
			} else {
				throw new IllegalArgumentException("Cannot start from a new item.");
			}
			return this;
		}

		private void checkAlreadyStarted() {
			if (intent.hasExtra(EXTRA_START_TYPE) || intent.hasExtra(EXTRA_START_ID)) {
				throw new IllegalStateException("You can only have one starting belonging.");
			}
		}

		public Intent build() {
			intent.putExtra(EXTRA_WHAT, what);
			if (!forbiddenPropertyIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_PROPERTIES, toArr(forbiddenPropertyIDs));
			}
			if (!forbiddenRoomIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_ROOMS, toArr(forbiddenRoomIDs));
			}
			if (!forbiddenItemIDs.isEmpty()) {
				intent.putExtra(EXTRA_NO_ITEMS, toArr(forbiddenItemIDs));
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
