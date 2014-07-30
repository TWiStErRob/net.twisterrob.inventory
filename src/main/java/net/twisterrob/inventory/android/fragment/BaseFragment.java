package net.twisterrob.inventory.android.fragment;

import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

public class BaseFragment<T> extends Fragment {
	protected static final String DYN_OptionsMenu = "optionsMenu";
	protected static final String DYN_Layout = "layoutRoot";
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
				throw new IllegalArgumentException("Activity must implement " + eventsClass);
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
		setHasOptionsMenu(hasDynResource(DYN_OptionsMenu));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(this.<Integer> getDynamicResource(DYN_Layout), container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onStartLoading();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
		if (hasDynResource(DYN_OptionsMenu)) {
			menuInflater.inflate(this.<Integer> getDynamicResource(DYN_OptionsMenu), menu);
		}
	}

	protected void onStartLoading() {}
}
