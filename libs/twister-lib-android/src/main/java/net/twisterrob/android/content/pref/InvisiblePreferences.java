package net.twisterrob.android.content.pref;

import java.lang.reflect.Method;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.preference.*;
import android.util.AttributeSet;

import static net.twisterrob.java.utils.ReflectionTools.*;

/**
 * A group of preferences that won't ever show up to the user.
 * If it's not part of the root {@link PreferenceScreen},
 * it is required to {@link #setDependency(String) set the dependency attribute} to the parent's key.
 *
 * @attr ref android.R.styleable#Preference_dependency
 * @see ValuePreference
 */
public class InvisiblePreferences extends PreferenceGroup {
	private static final Logger LOG = LoggerFactory.getLogger(InvisiblePreferences.class);

	private static final Method getPreferenceScreen =
			trySetAccessible(tryFindDeclaredMethod(PreferenceManager.class, "getPreferenceScreen"));

	public InvisiblePreferences(Context context) {
		super(context, null);
	}
	public InvisiblePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public InvisiblePreferences(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public InvisiblePreferences(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override protected boolean onPrepareAddPreference(Preference preference) {
		// cannot return false here, because this gets called even when just the PreferenceManager model is built
		// which is required to happen in order to be recognized when used with PreferenceManager.setDefaultValues
		return super.onPrepareAddPreference(preference);
	}

	@Override public boolean isEnabled() {
		return false;
	}

	@Override public boolean shouldDisableDependents() {
		return !super.isEnabled();
	}

	@Override protected void onAttachedToActivity() {
		try {
			String dependency = getDependency();
			PreferenceGroup parent;
			if (dependency == null) {
				//noinspection ConstantConditions null access is ok, there's a catch; also it should be null
				parent = (PreferenceGroup)getPreferenceScreen.invoke(getPreferenceManager());
			} else {
				parent = (PreferenceGroup)getPreferenceManager().findPreference(dependency);
			}
			parent.removePreference(this);
		} catch (Exception e) {
			LOG.warn("Cannot get parent to remove " + this, e);
		}
		super.onAttachedToActivity();
	}
}
