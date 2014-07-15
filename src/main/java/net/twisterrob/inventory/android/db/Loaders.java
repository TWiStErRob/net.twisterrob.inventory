package net.twisterrob.inventory.android.db;

import android.content.Context;
import android.database.*;
import android.os.Bundle;
import android.support.v4.content.Loader;

import net.twisterrob.android.content.loader.SimpleCursorLoader;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;

public enum Loaders {
	PropertyTypes {
		@Override
		protected Cursor createCursor(Bundle args) {
			return App.getInstance().getDataBase().listPropertyTypes();
		}
	},
	Properties {
		@Override
		protected Cursor createCursor(Bundle args) {
			return App.getInstance().getDataBase().listProperties();
		}
	},
	SingleProperty {
		public static final String EXTRA_PROPERTY_ID = PropertyEditActivity.EXTRA_PROPERTY_ID;
		@Override
		protected Cursor createCursor(Bundle args) {
			long id = args.getLong(EXTRA_PROPERTY_ID, Property.ID_ADD);
			return App.getInstance().getDataBase().getProperty(id);
		}
	},
	RoomTypes {
		@Override
		protected Cursor createCursor(Bundle args) {
			return App.getInstance().getDataBase().listRoomTypes();
		}
	},
	Rooms {
		public static final String EXTRA_PROPERTY_ID = RoomsActivity.EXTRA_PROPERTY_ID;
		@Override
		protected Cursor createCursor(Bundle args) {
			long id = args.getLong(EXTRA_PROPERTY_ID, Room.ID_ADD);
			return App.getInstance().getDataBase().listRooms(id);
		}
	},
	SingleRoom {
		public static final String EXTRA_ROOM_ID = RoomEditActivity.EXTRA_ROOM_ID;
		@Override
		protected Cursor createCursor(Bundle args) {
			long id = args.getLong(EXTRA_ROOM_ID, Room.ID_ADD);
			return App.getInstance().getDataBase().getRoom(id);
		}
	};

	private static final Bundle NO_ARGS = new Bundle(0);

	protected abstract Cursor createCursor(Bundle args);

	public Loader<Cursor> createLoader(Context context, final Bundle args) {
		return new SimpleCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				return createCursor(args != null? args : NO_ARGS);
			}
			@Override
			public void deliverResult(Cursor cursor) {
				try {
					super.deliverResult(cursor);
				} catch (RuntimeException ex) {
					DatabaseUtils.dumpCursor(cursor);
					throw ex;
				}
			}
		};
	}

	public static Loaders fromID(int id) {
		return values()[id];
	}
}
