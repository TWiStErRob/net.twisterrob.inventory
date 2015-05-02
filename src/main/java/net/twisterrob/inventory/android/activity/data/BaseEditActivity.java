package net.twisterrob.inventory.android.activity.data;

import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.fragment.data.BaseEditFragment;

import static net.twisterrob.inventory.android.fragment.data.BaseEditFragment.*;

public abstract class BaseEditActivity<E extends BaseEditFragment> extends BaseActivity {
	private E editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.generic_activity_nodrawer);

		if (savedInstanceState == null) {
			editor = onCreateFragment(null);
			editor.getArguments().putBoolean(EDIT_IMAGE, getIntent().getBooleanExtra(EDIT_IMAGE, false));
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.activityRoot, editor)
			                           .commit()
			;
		} else {
			editor = getFragment(R.id.activityRoot);
		}
	}

	@Override public void onBackPressed() {
		if (editor.isDirty()) {
			new AlertDialog.Builder(this)
					.setTitle("Unsaved changes")
					.setMessage("Continuing will discard any changes.")
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							BaseEditActivity.super.onBackPressed();
						}
					})
					.setNeutralButton(R.string.action_save, new OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							editor.save();
						}
					})
					.create()
					.show()
			;
		} else {
			super.onBackPressed();
		}
	}

	@Override public boolean onSupportNavigateUp() {
		super.onBackPressed(); // escape activity without dirty check
		return true;
	}

	protected abstract E onCreateFragment(Bundle savedInstanceState);

	public E getEditor() {
		return editor;
	}

	public static Intent takeImage(Intent edit) {
		edit.putExtra(EDIT_IMAGE, true);
		return edit;
	}
}
