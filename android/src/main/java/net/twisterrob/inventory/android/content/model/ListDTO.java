package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import androidx.annotation.NonNull;

public class ListDTO extends DTO {
	public static ListDTO fromCursor(@NonNull Cursor cursor) {
		ListDTO list = new ListDTO();
		return (ListDTO)list.fromCursorInternal(cursor);
	}

	@Override public @NonNull String toString() {
		return String.format(Locale.ROOT, "List #%1$d: '%2$s'", id, name);
	}
}
