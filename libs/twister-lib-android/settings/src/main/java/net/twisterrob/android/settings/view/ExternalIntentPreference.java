package net.twisterrob.android.settings.view;

import java.util.List;

import android.content.Context;
import android.content.pm.*;
import android.util.AttributeSet;

import androidx.preference.Preference;

/**
 * Simple stand-in for {@link Preference} which only has an {@code <intent>} in it.
 * Instead of crashing, automatically disable the click-ability
 * when the {@link android.content.Intent} is not resolvable.
 */
public class ExternalIntentPreference extends Preference {

	public ExternalIntentPreference(Context context) {
		super(context);
	}
	public ExternalIntentPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ExternalIntentPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public ExternalIntentPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override public void onAttached() {
		super.onAttached();
		PackageManager pm = getContext().getPackageManager();
		List<ResolveInfo> intents = pm.queryIntentActivities(getIntent(), 0);
		setEnabled(!intents.isEmpty());
	}
}
