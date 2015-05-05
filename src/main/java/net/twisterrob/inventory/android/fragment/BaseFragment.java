package net.twisterrob.inventory.android.fragment;

import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;

public class BaseFragment<T> extends VariantFragment {
	private static final Logger LOG = LoggerFactory.getLogger(BaseFragment.class);

	protected static final String DYN_OptionsMenu = "optionsMenu";
	protected static final String DYN_EventsClass = "eventsListenerClass";
	private static final String KEY_VIEWTAG = "BaseFragment.viewTag";

	private Map<String, Object> dynResources = new HashMap<>();
	private Parcelable tag;

	protected T eventsListener;

	@SuppressWarnings("unchecked")
	public <P extends Parcelable> P getViewTag() {
		return (P)tag;
	}

	public void setViewTag(Parcelable tag) {
		this.tag = tag;
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
		if (eventsClass != null) {
			eventsListener = AndroidTools.findAttachedListener(this, eventsClass);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		eventsListener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.debug("Creating {}@{} {}",
				getClass().getSimpleName(),
				Integer.toHexString(System.identityHashCode(this)),
				AndroidTools.toLongString(getArguments())
		);
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			tag = savedInstanceState.getParcelable(KEY_VIEWTAG);
		}
		setHasOptionsMenu(true);
	}

	public boolean hasUI() {
		return true;
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

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_VIEWTAG, tag);
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

	protected final Context getContext() {
		return getActivity();
	}
}
