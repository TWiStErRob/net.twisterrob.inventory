package net.twisterrob.inventory.android.fragment;

import java.util.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.*;
import android.view.*;

import androidx.annotation.*;
import androidx.fragment.app.FragmentActivity;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.base.BuildConfig;

public class BaseFragment<T> extends VariantFragment {
	private static final Logger LOG = LoggerFactory.getLogger(BaseFragment.class);

	protected static final String DYN_OptionsMenu = "optionsMenu";
	protected static final String DYN_EventsClass = "eventsListenerClass";
	private static final String KEY_VIEWTAG = "BaseFragment.viewTag";

	private final Map<String, Object> dynResources = new HashMap<>();
	private Parcelable tag;

	protected T eventsListener;

	public BaseFragment() {
		super();
		// prevent headaches when querying arguments (see Main.onCreateOptionsMenu)
		setArguments(null);
	}

	@ContentView
	public BaseFragment(@LayoutRes int contentLayoutId) {
		super(contentLayoutId);
	}

	@Override public void setArguments(@Nullable Bundle args) {
		super.setArguments(args != null? args : new Bundle());
	}

	public @Nullable Parcelable getViewTag() {
		return tag;
	}

	public void setViewTag(@Nullable Parcelable tag) {
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

	public void setEventsListener(T eventsListener) {
		this.eventsListener = eventsListener;
	}

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		Class<T> eventsClass = getDynamicResource(DYN_EventsClass);
		if (eventsClass != null) {
			setEventsListener(AndroidTools.findAttachedListener(this, eventsClass));
		}
	}

	@Override public void onDetach() {
		super.onDetach();
		setEventsListener(null);
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			LOG.debug("Creating {}@{} {}",
					getClass().getSimpleName(),
					Integer.toHexString(System.identityHashCode(this)),
					StringerTools.toString(getArguments())
			);
		}
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			tag = BundleTools.getParcelable(savedInstanceState, KEY_VIEWTAG, Parcelable.class);
		}
		setHasOptionsMenu(true);
	}

	public boolean hasUI() {
		return true;
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/255
	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onStartLoading();
	}

	@Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (hasDynResource(DYN_OptionsMenu)) {
			inflater.inflate(this.<Integer>getDynamicResource(DYN_OptionsMenu), menu);
		}
	}

	/**
	 * Non-restricted API to check for options menu,
	 * could be simply re-implemented by overriding {@link #setHasOptionsMenu(boolean)}, but pointless.
	 */
	@SuppressLint("RestrictedApi")
	public boolean hasSetOptionsMenu() {
		return hasOptionsMenu();
	}

	public void invalidateOptionsMenu() {
		if (!hasSetOptionsMenu()) {
			return;
		}
		FragmentActivity activity = getActivity();
		if (activity != null) {
			activity.invalidateOptionsMenu();
		}
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_VIEWTAG, tag);
	}

	protected void onStartLoading() {
		// optional override
	}

	public final void refresh() {
		// FIXME remove getHost() check and fix action calling refresh() a rotated dead fragment, see Dialogs.class
		if (getHost() != null && !getLoaderManager().hasRunningLoaders()) {
			onRefresh();
		}
	}

	protected void onRefresh() {
		// optional override
	}
}
