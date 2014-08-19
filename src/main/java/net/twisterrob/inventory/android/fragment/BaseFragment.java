package net.twisterrob.inventory.android.fragment;

import java.util.*;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.activity.BaseActivity;

public class BaseFragment<T> extends Fragment {
	protected static final String DYN_OptionsMenu = "optionsMenu";
	protected static final String DYN_EventsClass = "eventsListenerClass";

	private Map<String, Object> dynResources = new HashMap<String, Object>();

	protected T eventsListener;

	protected void setDynamicResource(String type, Object resource) {
		dynResources.put(type, resource);
	}
	protected boolean hasDynResource(String type) {
		return dynResources.containsKey(type);
	}
	protected <D> D getDynamicResource(String type) {
		return (D)dynResources.get(type);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Class<T> eventsClass = this.<Class<T>> getDynamicResource(DYN_EventsClass);
		eventsListener = AndroidTools.getAttachedFragmentListener(activity, eventsClass);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		eventsListener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onStartLoading();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (hasDynResource(DYN_OptionsMenu)) {
			inflater.inflate(this.<Integer> getDynamicResource(DYN_OptionsMenu), menu);
		}
	}

	protected void onStartLoading() {
		// optional override
	}

	public final void refresh() {
		if (!getLoaderManager().hasRunningLoaders()) {
			onRefresh();
		}
	}

	protected void onRefresh() {
		// optional override
	}

	public void setTitle(CharSequence string) {
		((BaseActivity)getActivity()).setActionBarTitle(string);
	}

	public void setIcon(Drawable iconDrawable) {
		((BaseActivity)getActivity()).setIcon(iconDrawable);
	}
}
