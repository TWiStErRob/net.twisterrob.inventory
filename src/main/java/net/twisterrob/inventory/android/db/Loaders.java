package net.twisterrob.inventory.android.db;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;

import net.twisterrob.android.db.SimpleCursorLoader;
import net.twisterrob.inventory.android.App;

public enum Loaders {
	BuildingTypes {
		@Override
		protected Cursor createCursor() {
			return App.getInstance().getDataBase().listBuildingTypes();
		}
	},
	Buildings {
		@Override
		protected Cursor createCursor() {
			return App.getInstance().getDataBase().listBuildings();
		}
	};

	protected abstract Cursor createCursor();

	public Loader<Cursor> createLoader(Context context) {
		return new SimpleCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				return createCursor();
			}
		};
	}

	public static Loaders fromID(int id) {
		return values()[id];
	}
}
