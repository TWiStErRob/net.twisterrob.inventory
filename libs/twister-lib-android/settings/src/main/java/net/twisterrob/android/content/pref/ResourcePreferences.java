package net.twisterrob.android.content.pref;

import java.util.*;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.*;

/**
 * I found it really useful to structure my preferences this way:
 * <ul>
 * 	<li>Create string resources for all titles and values</li>
 * 	<li>References these in arrays</li>
 * 	<li>References these in code</li>
 * </ul>
 * This has a lot of potential benefits:
 * <ul>
 * 	<li>Allows for using different default values per screen/device/language/etc.</li>
 * 	<li>Allows for not hard-coding anything in code</li>
 * 	<li>Allows to document everything in one place</li>
 * 	<li>Allows for searching for usages of each preference</li>
 * 	<li>Allows for searching for usages of each preference value</li>
 * </ul>
 *
 * Here's the definition of a sample setting (single choice of 3 values):
 * <pre><code>
 * &lt;!-- Description of Some Setting --&gt;
 * &lt;string name="pref$someSetting$title"&gt;Some Setting&lt;/string&gt;
 * &lt;string name="pref$someSetting" translatable="false"&gt;someSetting&lt;/string&gt;
 * &lt;string name="pref$someSetting$default" translatable="false"&gt;@string/pref$someSetting_option2&lt;/string&gt;
 * &lt;!-- Description of Option 1 --&gt;
 * &lt;string name="pref$someSetting_option1$title"&gt;Option 1&lt;/string&gt;
 * &lt;string name="pref$someSetting_option1" translatable="false"&gt;option1&lt;/string&gt;
 * &lt;!-- Description of Option 2 --&gt;
 * &lt;string name="pref$someSetting_option2$title"&gt;Option 2&lt;/string&gt;
 * &lt;string name="pref$someSetting_option2" translatable="false"&gt;option2&lt;/string&gt;
 * &lt;!-- Description of Option 3 --&gt;
 * &lt;string name="pref$someSetting_option3$title"&gt;Option 3&lt;/string&gt;
 * &lt;string name="pref$someSetting_option3" translatable="false"&gt;option3&lt;/string&gt;
 * &lt;string-array name="pref$someSetting_entries"&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option1$title&lt;/item&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option2$title&lt;/item&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option3$title&lt;/item&gt;
 * &lt;/string-array&gt;
 * &lt;string-array name="pref$someSetting_values" translatable="false"&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option1&lt;/item&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option2&lt;/item&gt;
 * 	&lt;item&gt;@string/pref$someSetting_option3&lt;/item&gt;
 * &lt;/string-array&gt;
 * </code></pre>
 *
 * Here's how it is used in a preference activity:
 * <pre><code>
 * &lt;ListPreference
 * 	android:key="@string/pref$someSetting"
 * 	android:title="@string/pref$someSetting$title"
 * 	android:defaultValue="@string/pref$someSetting$default"
 * 	android:entries="@array/pref$someSetting_entries"
 * 	android:entryValues="@array/pref$someSetting_values"
 * 	tools:summary="@array/pref$someSetting_entries"
 * 	/&gt;
 * </code></pre>
 *
 * Here's how it can be used with this class:
 * <pre><code>
 * private ResourcePreferences prefs;
 *
 * String currentValue = prefs.getString(R.string.pref$someSetting, R.string.pref$someSetting$default);
 * String option1 = context.getString(R.string.pref$someSetting_option1);
 * if (option1.equals(currentValue)) {
 * 	// behave according to option1, for example change the value to option2
 * 	prefs.setString(R.string.pref$someSetting, R.string.pref$someSetting_option2);
 * }
 * </code></pre>
 *
 * All of the methods are named the same as the original ones, except
 * {@link #getIntVal(int, int)}, {@link #setIntVal(int, int)}, {@link Editor#putInt(int, int)}. 
 * The above need a different name, because {@link IntegerRes} is not part of the Java method signature.
 */
public class ResourcePreferences extends SharedPreferencesWrapper {
	private final Resources res;
	public ResourcePreferences(Resources res, SharedPreferences prefs) {
		super(prefs);
		this.res = res;
	}

	@SuppressLint("CommitPrefEdits")
	@Override public ResourcePreferences.Editor edit() {
		return new ResourcePreferences.Editor(res, super.edit());
	}

	public boolean contains(@StringRes int prefName) {
		String prefKey = res.getString(prefName);
		return super.contains(prefKey);
	}

	public String getString(@StringRes int prefName, String defValue) {
		String prefKey = res.getString(prefName);
		return super.getString(prefKey, defValue);
	}
	public String getString(@StringRes int prefName, @StringRes int defResource) {
		String prefKey = res.getString(prefName);
		String prefDefaultValue = res.getString(defResource);
		return super.getString(prefKey, prefDefaultValue);
	}

	public Set<String> getStringSet(@StringRes int prefName, Set<String> defValue) {
		String prefKey = res.getString(prefName);
		return super.getStringSet(prefKey, defValue);
	}
	public Set<String> getStringSet(@StringRes int prefName, @ArrayRes int defResource) {
		String prefKey = res.getString(prefName);
		String[] prefDefaultResourceValue = res.getStringArray(defResource);
		Set<String> prefDefaultValue =
				prefDefaultResourceValue != null? new HashSet<>(Arrays.asList(prefDefaultResourceValue)) : null;
		return super.getStringSet(prefKey, prefDefaultValue);
	}

	public int getIntVal(@StringRes int prefName, int defValue) {
		String prefKey = res.getString(prefName);
		return super.getInt(prefKey, defValue);
	}
	public int getInt(@StringRes int prefName, @IntegerRes int defResource) {
		String prefKey = res.getString(prefName);
		int prefDefault = res.getInteger(defResource);
		return super.getInt(prefKey, prefDefault);
	}

	public long getLong(@StringRes int prefName, long defValue) {
		String prefKey = res.getString(prefName);
		return super.getLong(prefKey, defValue);
	}
	public long getLong(@StringRes int prefName, @IntegerRes int defResource) {
		String prefKey = res.getString(prefName);
		int prefDefault = res.getInteger(defResource);
		return super.getLong(prefKey, prefDefault);
	}

	public float getFloat(@StringRes int prefName, float defValue) {
		String prefKey = res.getString(prefName);
		return super.getFloat(prefKey, defValue);
	}
	public float getFloat(@StringRes int prefName, @DimenRes int defResource) {
		String prefKey = res.getString(prefName);
		float prefDefault = res.getDimension(defResource);
		return super.getFloat(prefKey, prefDefault);
	}

	public boolean getBoolean(@StringRes int prefName, boolean defValue) {
		String prefKey = res.getString(prefName);
		return super.getBoolean(prefKey, defValue);
	}
	public boolean getBoolean(@StringRes int prefName, @BoolRes int defResource) {
		String prefKey = res.getString(prefName);
		boolean prefDefault = res.getBoolean(defResource);
		return super.getBoolean(prefKey, prefDefault);
	}

	public void setString(@StringRes int prefName, @StringRes int resource) {
		edit().putString(prefName, resource).apply();
	}
	public void setString(@StringRes int prefName, @Nullable String value) {
		edit().putString(prefName, value).apply();
	}

	public void setStringSet(@StringRes int prefName, @ArrayRes int resource) {
		edit().putStringSet(prefName, resource).apply();
	}
	public void setStringSet(@StringRes int prefName, @Nullable Set<String> values) {
		edit().putStringSet(prefName, values).apply();
	}

	public void setInt(@StringRes int prefName, @IntegerRes int resource) {
		edit().putInt(prefName, resource).apply();
	}
	public void setIntVal(@StringRes int prefName, int value) {
		edit().putIntVal(prefName, value).apply();
	}

	public void setLong(@StringRes int prefName, @IntegerRes int resource) {
		edit().putLong(prefName, resource).apply();
	}
	public void setLong(@StringRes int prefName, long value) {
		edit().putLong(prefName, value).apply();
	}

	public void setFloat(@StringRes int prefName, @DimenRes int resource) {
		edit().putFloat(prefName, resource).apply();
	}
	public void setFloat(@StringRes int prefName, float value) {
		edit().putFloat(prefName, value).apply();
	}

	public void setBoolean(@StringRes int prefName, @BoolRes int resource) {
		edit().putBoolean(prefName, resource).apply();
	}
	public void setBoolean(@StringRes int prefName, boolean value) {
		edit().putBoolean(prefName, value).apply();
	}

	public static class Editor extends SharedPreferencesWrapper.Editor<Editor> {
		private final Resources res;
		public Editor(Resources res, SharedPreferences.Editor editor) {
			super(editor);
			this.res = res;
		}

		public Editor remove(@StringRes int prefName) {
			String prefKey = res.getString(prefName);
			return super.remove(prefKey);
		}

		public Editor putString(@StringRes int prefName, @StringRes int resource) {
			String prefKey = res.getString(prefName);
			String value = res.getString(resource);
			return super.putString(prefKey, value);
		}
		public Editor putString(@StringRes int prefName, @Nullable String value) {
			String prefKey = res.getString(prefName);
			return super.putString(prefKey, value);
		}

		public Editor putStringSet(@StringRes int prefName, int resource) {
			String prefKey = res.getString(prefName);
			String[] resourceValues = res.getStringArray(resource);
			Set<String> values =
					resourceValues != null? new HashSet<>(Arrays.asList(resourceValues)) : null;
			return super.putStringSet(prefKey, values);
		}
		public Editor putStringSet(@StringRes int prefName, @Nullable Set<String> values) {
			String prefKey = res.getString(prefName);
			return super.putStringSet(prefKey, values);
		}

		public Editor putInt(@StringRes int prefName, @IntegerRes int resource) {
			String prefKey = res.getString(prefName);
			int value = res.getInteger(resource);
			return super.putInt(prefKey, value);
		}
		public Editor putIntVal(@StringRes int prefName, int value) {
			String prefKey = res.getString(prefName);
			return super.putInt(prefKey, value);
		}

		public Editor putLong(@StringRes int prefName, @IntegerRes int resource) {
			String prefKey = res.getString(prefName);
			int value = res.getInteger(resource);
			return super.putLong(prefKey, value);
		}
		public Editor putLong(@StringRes int prefName, long value) {
			String prefKey = res.getString(prefName);
			return super.putLong(prefKey, value);
		}

		public Editor putFloat(@StringRes int prefName, @DimenRes int resource) {
			String prefKey = res.getString(prefName);
			float value = res.getDimension(resource);
			return super.putFloat(prefKey, value);
		}
		public Editor putFloat(@StringRes int prefName, float value) {
			String prefKey = res.getString(prefName);
			return super.putFloat(prefKey, value);
		}

		public Editor putBoolean(@StringRes int prefName, @BoolRes int resource) {
			String prefKey = res.getString(prefName);
			boolean value = res.getBoolean(resource);
			return super.putBoolean(prefKey, value);
		}
		public Editor putBoolean(@StringRes int prefName, boolean value) {
			String prefKey = res.getString(prefName);
			return super.putBoolean(prefKey, value);
		}
	}
}
