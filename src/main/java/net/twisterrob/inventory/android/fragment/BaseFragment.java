package net.twisterrob.inventory.android.fragment;

import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

import net.twisterrob.inventory.R;

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
		if (eventsClass != null) {
			if (eventsClass.isInstance(activity)) {
				eventsListener = eventsClass.cast(activity);
			} else {
				throw new IllegalArgumentException("Activity " + activity.getClass().getSimpleName()
						+ " must implement " + eventsClass);
			}
		}
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
		inflater.inflate(R.menu.share, menu);
	}

	protected void onStartLoading() {
		// optional override
	}

	public final void refresh() {
		onRefresh();
	}

	protected void onRefresh() {
		// optional override
	}
}
