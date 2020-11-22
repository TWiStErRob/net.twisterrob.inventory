package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.model.DTO;

public abstract class RenameAction<T extends DTO> extends BaseAction {
	@Input private final int typeRes;
	@Input protected final long id;

	@Prepared protected T dto;
	private EditText newNameEditor;
	@Input private String newName;

	public RenameAction(long id, int typeRes) {
		this.id = id;
		this.typeRes = typeRes;
	}

	public RenameAction(long id, String newName, int typeRes) {
		this(id, typeRes);
		this.newName = newName;
	}

	@Override public View getConfirmationView(@NonNull Context context) {
		newNameEditor = new EditText(context);
		newNameEditor.setSingleLine(true);
		newNameEditor.setText(newName != null? newName : dto.name);
		newNameEditor.setSelection(newNameEditor.getText().length());
		AndroidTools.showKeyboard(newNameEditor);
		return newNameEditor;
	}

	protected String getNewName() {
		return newNameEditor != null? newNameEditor.getText().toString() : newName;
	}

	@Override protected @NonNull CharSequence getGenericFailureMessage(@NonNull Resources res) {
		String oldName = dto != null? dto.name : "?";
		return res.getString(R.string.action_rename_failed, res.getQuantityString(typeRes, 1), oldName, getNewName());
	}
	@Override public @NonNull CharSequence getConfirmationTitle(@NonNull Resources res) {
		return res.getString(R.string.action_rename_title, res.getQuantityString(typeRes, 1), dto.name);
	}
	@Override public @NonNull CharSequence getConfirmationMessage(@NonNull Resources res) {
		return res.getString(R.string.action_rename_ask, res.getQuantityString(typeRes, 1), dto.name);
	}
	@Override public @NonNull CharSequence getSuccessMessage(@NonNull Resources res) {
		return res.getString(R.string.action_rename_success, res.getQuantityString(typeRes, 1), dto.name, newName);
	}
}
