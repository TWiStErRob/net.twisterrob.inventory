package net.twisterrob.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;

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
