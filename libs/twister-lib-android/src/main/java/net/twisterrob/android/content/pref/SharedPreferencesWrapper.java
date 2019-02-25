package net.twisterrob.android.content.pref;

import java.util.*;

import android.annotation.*;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;

/**
 * Base class for creating custom SharedPreferences objects.
 * It delegates all calls to the preferences given in the constructor.
 */
public class SharedPreferencesWrapper implements SharedPreferences {
	private final SharedPreferences wrapped;
	private final Map<OnSharedPreferenceChangeListener, OnSharedPreferenceChangeListener> listeners =
			new WeakHashMap<>();

	public SharedPreferencesWrapper(SharedPreferences prefs) {
		this.wrapped = prefs;
	}

	/** @return You must call {@link SharedPreferences.Editor#commit} as per {@link SharedPreferences#edit} contract. */
	@SuppressLint("CommitPrefEdits")
	public SharedPreferences.Editor edit() {
		return wrapped.edit();
	}

	public Map<String, ?> getAll() {
		return wrapped.getAll();
	}

	public @Nullable String getString(String key, @Nullable String defValue) {
		return wrapped.getString(key, defValue);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public @Nullable Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
		return wrapped.getStringSet(key, defValues);
	}

	public int getInt(String key, int defValue) {
		return wrapped.getInt(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return wrapped.getLong(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return wrapped.getFloat(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return wrapped.getBoolean(key, defValue);
	}

	public boolean contains(String key) {
		return wrapped.contains(key);
	}

	public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
		OnSharedPreferenceChangeListener wrapper = listeners.get(listener);
		if (wrapper == null) {
			wrapper = new OnSharedPreferenceChangeListener() {
				@Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					firePreferenceChanged(sharedPreferences, key, listener);
				}
			};
			listeners.put(listener, wrapper);
		}
		wrapped.registerOnSharedPreferenceChangeListener(wrapper);
	}

	public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
		wrapped.unregisterOnSharedPreferenceChangeListener(listeners.get(listener));
	}

	protected void firePreferenceChanged(
			SharedPreferences sharedPreferences, String key, OnSharedPreferenceChangeListener listener) {
		// STOPSHIP is this intentionally ignoring param?
		listener.onSharedPreferenceChanged(this, key);
	}

	/**
	 * Base class for creating custom editors,
	 * it's not used by default by the preferences wrapper to prevent a layer of unnecessary indirection.
	 * A simple example of extending this class:
	 * {@code class MyEditor extends SharedPreferencesWrapper.Editor<MyEditor>}
	 *
	 * @param <E> must be the concrete class extending this class to maintain the builder pattern
	 */
	@SuppressWarnings("unchecked")
	public static class Editor<E extends SharedPreferences.Editor> implements SharedPreferences.Editor {
		private final SharedPreferences.Editor wrapped;

		public Editor(SharedPreferences.Editor editor) {
			this.wrapped = editor;
		}

		public E putString(String key, String value) {
			wrapped.putString(key, value);
			return (E)this;
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public E putStringSet(String key, Set<String> values) {
			wrapped.putStringSet(key, values);
			return (E)this;
		}

		public E putInt(String key, int value) {
			wrapped.putInt(key, value);
			return (E)this;
		}

		public E putLong(String key, long value) {
			wrapped.putLong(key, value);
			return (E)this;
		}

		public E putFloat(String key, float value) {
			wrapped.putFloat(key, value);
			return (E)this;
		}

		public E putBoolean(String key, boolean value) {
			wrapped.putBoolean(key, value);
			return (E)this;
		}

		public E remove(String key) {
			wrapped.remove(key);
			return (E)this;
		}

		public E clear() {
			wrapped.clear();
			return (E)this;
		}

		public boolean commit() {
			return wrapped.commit();
		}

		public void apply() {
			wrapped.apply();
		}
	}
}
