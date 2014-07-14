package com.example.android.xmladapters;

import android.content.Context;
import android.database.Cursor;

import com.example.android.xmladapters.Adapters.CursorTransformation;

public class TranslateTransformation extends CursorTransformation {
	public TranslateTransformation(Context context) {
		super(context);
	}

	@Override
	public String transform(Cursor cursor, int columnIndex) {
		return mContext.getString(transformToResource(cursor, columnIndex));
	}

	@Override
	public int transformToResource(Cursor cursor, int columnIndex) {
		String data = cursor.getString(columnIndex);
		return mContext.getResources().getIdentifier(data, "string", mContext.getPackageName());
	}
}
