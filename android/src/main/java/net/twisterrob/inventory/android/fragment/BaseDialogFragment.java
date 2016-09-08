package net.twisterrob.inventory.android.fragment;

import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {
	@Override public void onDestroyView() {
		// http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
		// https://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}
}
