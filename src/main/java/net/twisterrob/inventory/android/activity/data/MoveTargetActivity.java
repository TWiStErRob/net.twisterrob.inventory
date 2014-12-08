package net.twisterrob.inventory.android.activity.data;

import java.util.*;

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

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
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
	/**
	 * What to search for, use {@link #PROPERTY}, {@link #ROOM} and {@link #ITEM} as flags,
	 * one of the flags will be the result code in {@link android.app.Activity#onActivityResult}.
	 */
	private static final String EXTRA_WHAT = "what";
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

		updateFragment(PropertyListFragment.newInstance());
	}

	private void updateFragment(@NonNull BaseFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.activityRoot, fragment);
		if (getFragment() != null) { // prevent backing up to empty dialog
			ft.addToBackStack(null);
		}
		ft.commit();
		updateUI(fragment);
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

	@Override public void propertySelected(long propertyID) {
		load(Loaders.SingleProperty, bundleFromProperty(propertyID), PROPERTY,
				RoomListFragment.newInstance(propertyID));
	}
	@Override public void roomSelected(long roomID) {
		load(Loaders.SingleRoom, bundleFromRoom(roomID), ROOM, ItemListFragment.newRoomInstance(roomID));
	}
	@Override public void itemSelected(long itemID) {
		load(Loaders.SingleItem, bundleFromItem(itemID), ITEM, ItemListFragment.newInstance(itemID));
	}
	private void load(final Loaders singleLoader, Bundle args, final int type, final BaseFragment fragment) {
		DynamicLoaderManager manager = new DynamicLoaderManager(getSupportLoaderManager());
		getSupportLoaderManager().destroyLoader(singleLoader.ordinal());
		Dependency<Cursor> data = manager.add(singleLoader.ordinal(), args, new LoadSingleRow(this) {
			@Override protected void process(Cursor cursor) {
				String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
				fragment.getArguments().putString(ARG_TITLE, name);
				boolean forbidden = isForbiddenData(cursor);
				if (forbidden) {
					fragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
				}
				// FragmentTransaction.commit: Can not perform this action inside of onLoadFinished
				// so must do it on the UI thread, but later!
				handler.post(new Runnable() {
					@Override public void run() {
						updateFragment(fragment);
					}
				});
			}
			private boolean isForbiddenData(Cursor cursor) {
				if (type == PROPERTY) {
					PropertyDTO property = PropertyDTO.fromCursor(cursor);
					return isForbidden(EXTRA_NO_PROPERTIES, property.id);
				} else if (type == ROOM) {
					RoomDTO room = RoomDTO.fromCursor(cursor);
					return isForbidden(EXTRA_NO_PROPERTIES, room.propertyID) || isForbidden(EXTRA_NO_ROOMS, room.id);
				} else {
					return false;
				}
			}
		});
		if (type == ITEM) {
			getSupportLoaderManager().destroyLoader(Loaders.ItemParents.ordinal());
			Dependency<Cursor> parents = manager.add(Loaders.ItemParents.ordinal(), args, new LoadersCallbacks(this) {
				@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
					while (data.moveToNext()) {
						long id = data.getLong(data.getColumnIndexOrThrow(ParentColumns.ID));
						String typeString = data.getString(data.getColumnIndexOrThrow(ParentColumns.TYPE));
						Type type = Type.from(typeString);
						if (type == Type.Property && isForbidden(EXTRA_NO_PROPERTIES, id)
								|| type == Type.Room && isForbidden(EXTRA_NO_ROOMS, id)
								|| type == Type.Item && isForbidden(EXTRA_NO_ITEMS, id)) {
							fragment.getArguments().putBoolean(ARG_FORBIDDEN, true);
						}
					}
				}
				@Override public void onLoaderReset(Loader<Cursor> loader) {
				}
			});
			data.dependsOn(parents);
		}
		manager.startLoading();
	}

	private boolean isForbidden(String extra, long id) {
		long[] ids = getIntent().getLongArrayExtra(extra);
		return 0 <= Arrays.binarySearch(ids, id);
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

		public Intent build() {
			intent.putExtra(EXTRA_WHAT, what);
			intent.putExtra(EXTRA_NO_PROPERTIES, toArr(propertyIDs));
			intent.putExtra(EXTRA_NO_ROOMS, toArr(roomIDs));
			intent.putExtra(EXTRA_NO_ITEMS, toArr(itemIDs));
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
