package net.twisterrob.inventory.android.fragment;

import androidx.fragment.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/258
	@Override public void onDestroyView() {
		// http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
		// https://issuetracker.google.com/issues/36929400
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}
}
