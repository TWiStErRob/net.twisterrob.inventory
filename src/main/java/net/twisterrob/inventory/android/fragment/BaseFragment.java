package net.twisterrob.inventory.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	protected static <T> T checkActivityInterface(Activity activity, Class<T> clazz) {
		//		if (clazz != null && clazz.isInstance(activity)) {
		return clazz.cast(activity);
		//		} else {
		//			throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.OnItemSelectedListener");
		//		}
	}
}
