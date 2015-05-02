package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.model.DTO;

public abstract class RenameAction<T extends DTO> extends BaseAction {
	private final int typeRes;
	protected final long id;

	protected T dto;
	private EditText newNameEditor;
	private String newName;

	public RenameAction(long id, int typeRes) {
		this.id = id;
		this.typeRes = typeRes;
	}

	public RenameAction(long id, String newName, int typeRes) {
		this(id, typeRes);
		this.newName = newName;
	}

	@Override public View getConfirmationView(Context context) {
		newNameEditor = new EditText(context);
		newNameEditor.setText(newName != null? newName : dto.name);
		return newNameEditor;
	}

	protected String getNewName() {
		return newNameEditor != null? newNameEditor.getText().toString() : newName;
	}

	@Override protected CharSequence getGenericFailureMessage(Resources res) {
		return res.getString(R.string.action_rename_failed, res.getQuantityString(typeRes, 1), dto.name, getNewName());
	}
	@Override public CharSequence getConfirmationTitle(Resources res) {
		return res.getString(R.string.action_rename_title, res.getQuantityString(typeRes, 1), dto.name);
	}
	@Override public CharSequence getConfirmationMessage(Resources res) {
		return res.getString(R.string.action_rename_ask, res.getQuantityString(typeRes, 1), dto.name);
	}
	@Override public CharSequence getSuccessMessage(Resources res) {
		return res.getString(R.string.action_rename_success, res.getQuantityString(typeRes, 1), dto.name, newName);
	}
}
