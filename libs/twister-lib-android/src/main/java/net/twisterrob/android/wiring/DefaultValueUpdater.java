package net.twisterrob.android.wiring;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import net.twisterrob.android.R;

public class DefaultValueUpdater implements OnItemSelectedListener {
	private static final int INVALID = -1;

	private final String columnName;
	private final EditText entityName;

	private int prevPosition = INVALID;

	public DefaultValueUpdater(EditText nameField, String cursorColumn) {
		this.entityName = nameField;
		this.columnName = cursorColumn;
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int oldName = prevPosition == INVALID? R.string.empty : getStringID(parent, prevPosition);
		int newName = getStringID(parent, position);

		String currentText = entityName.getText().toString().trim();
		String oldAsText = parent.getContext().getString(oldName);
		if (currentText.isEmpty() || currentText.equals(oldAsText)) {
			entityName.setText(newName);
		}

		prevPosition = position;
	}

	@SuppressWarnings("resource")
	private int getStringID(AdapterView<?> parent, int position) {
		Cursor newData = (Cursor)parent.getAdapter().getItem(position);
		String newName = newData.getString(newData.getColumnIndexOrThrow(columnName));
		Context context = parent.getContext();
		return context.getResources().getIdentifier(newName, "string", context.getPackageName());
	}

	public void onNothingSelected(AdapterView<?> parent) {
		// ignore
	}
}