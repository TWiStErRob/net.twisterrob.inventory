package net.twisterrob.inventory.android.fragment;

import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.activity.BaseActivity;

public class BaseFragment<T> extends Fragment {
	private static final Logger LOG = LoggerFactory.getLogger(BaseFragment.class);

	protected static final String DYN_OptionsMenu = "optionsMenu";
	protected static final String DYN_EventsClass = "eventsListenerClass";

	private Map<String, Object> dynResources = new HashMap<>();

	protected T eventsListener;

	public BaseFragment() {
		LOG.trace("Creating {}@{}",
				getClass().getSimpleName(),
				Integer.toHexString(System.identityHashCode(this))
		);
	}

	protected void setDynamicResource(String type, Object resource) {
		dynResources.put(type, resource);
	}
	protected boolean hasDynResource(String type) {
		return dynResources.containsKey(type);
	}
	@SuppressWarnings("unchecked")
	protected <D> D getDynamicResource(String type) {
		return (D)dynResources.get(type);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Class<T> eventsClass = getDynamicResource(DYN_EventsClass);
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
			inflater.inflate(this.<Integer>getDynamicResource(DYN_OptionsMenu), menu);
		}
	}

	protected void showDialog(DialogFragment dialog) {
		dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
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

	protected BaseActivity getBaseActivity() {
		return (BaseActivity)super.getActivity();
	}

	protected final Context getContext() {
		return getActivity();
	}
}
