package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.*;

import android.content.Context;
import android.view.View;

class ViewIDKeyFormatter implements KeyFormatter {
	/** @see View#toString() */
	private static final int RESOURCE_ID_MASK = 0xff000000;

	private static final Collection<String> RESOLVE_RESOURCE_ID_KEYS = new HashSet<>(Arrays.asList(
			"android:views", // savedInstanceState > android:viewHierarchyState
			"android:view_state",
			// (FragmentManagerState)android:support:fragments > FragmentManagerImpl.VIEW_STATE_TAG
			"android:menu:action_views", // savedInstanceState > NavigationView.SavedState
			"android:focusedViewId"
	));

	private final Context appContext;
	public ViewIDKeyFormatter(Context context) {
		appContext = context.getApplicationContext();
	}
	@Override public boolean needResolveIDs(Object key, Object value) {
		boolean keyNeedsResolving = appContext != null && RESOLVE_RESOURCE_ID_KEYS.contains(String.valueOf(key));
		boolean keyCanBeResolved = key instanceof Integer && ((Integer)key & RESOURCE_ID_MASK) != 0;
		boolean valueCanBeResolved = value instanceof Integer && ((Integer)value & RESOURCE_ID_MASK) != 0;
		return keyNeedsResolving || keyCanBeResolved || valueCanBeResolved;
	}
}
