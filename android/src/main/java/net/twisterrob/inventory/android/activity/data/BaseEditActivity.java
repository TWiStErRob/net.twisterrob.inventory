package net.twisterrob.inventory.android.activity.data;

import android.content.*;
import android.content.DialogInterface.OnClickListener;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.SingleFragmentActivity;
import net.twisterrob.inventory.android.fragment.data.BaseEditFragment;

import static net.twisterrob.inventory.android.fragment.data.BaseEditFragment.*;

public abstract class BaseEditActivity<E extends BaseEditFragment<?, ?>> extends SingleFragmentActivity<E> {
	@Override protected FragmentTransaction updateFragment(E fragment) {
		fragment.requireArguments().putBoolean(EDIT_IMAGE, getIntent().getBooleanExtra(EDIT_IMAGE, false));
		return super.updateFragment(fragment);
	}

	@Override public void onBackPressed() {
		if (getFragment().isDirty()) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.generic_edit_dirty_title)
					.setMessage(R.string.generic_edit_dirty_message)
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
		onBackPressed(); // CONSIDER escape activity without dirty check (add `super.`)
		// Sarah was using up as back. I was thinking as up nav from edit as ESC (don't save). Who is right?
		return true;
	}

	public static Intent editImage(Intent edit) {
		edit.putExtra(EDIT_IMAGE, true);
		return edit;
	}
}
