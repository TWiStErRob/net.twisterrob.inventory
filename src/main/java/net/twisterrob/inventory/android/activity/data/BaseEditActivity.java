package net.twisterrob.inventory.android.activity.data;

import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentTransaction;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.SingleFragmentActivity;
import net.twisterrob.inventory.android.fragment.data.BaseEditFragment;

import static net.twisterrob.inventory.android.fragment.data.BaseEditFragment.*;

public abstract class BaseEditActivity<E extends BaseEditFragment<?, ?>> extends SingleFragmentActivity<E> {
	@Override protected FragmentTransaction updateFragment(E fragment) {
		fragment.getArguments().putBoolean(EDIT_IMAGE, getIntent().getBooleanExtra(EDIT_IMAGE, false));
		return super.updateFragment(fragment);
	}

	@Override public void onBackPressed() {
		if (getFragment().isDirty()) {
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
							getFragment().save();
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

	public static Intent editImage(Intent edit) {
		edit.putExtra(EDIT_IMAGE, true);
		return edit;
	}
}
