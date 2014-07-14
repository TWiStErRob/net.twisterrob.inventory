package net.twisterrob.inventory.android.activity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import net.twisterrob.inventory.R;

public class DefaultValueUpdater implements OnItemSelectedListener {
	private static final int INVALID = -1;
	private int prevPosition = INVALID;

	private final EditText roomName;
	private final String columnName;
	
	public DefaultValueUpdater(EditText roomName, String columnName) {
		this.roomName = roomName;
		this.columnName = columnName;
	}
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int oldName = prevPosition == INVALID? R.string.empty : getStringID(parent, prevPosition);
		int newName = getStringID(parent, position);

		String currentText = roomName.getText().toString().trim();
		String oldAsText = parent.getContext().getString(oldName);
		if (currentText.isEmpty() || currentText.equals(oldAsText)) {
			roomName.setText(newName);
		}

		prevPosition = position;
	}
	@SuppressWarnings("resource")
	private int getStringID(AdapterView<?> parent, int position) {
		Cursor newData = (Cursor)parent.getAdapter().getItem(position);
		String newName = newData.getString(newData.getColumnIndex(columnName));
		Context context = parent.getContext();
		return context.getResources().getIdentifier(newName, "string", context.getPackageName());
	}
	public void onNothingSelected(AdapterView<?> parent) {
		// ignore
	}
}