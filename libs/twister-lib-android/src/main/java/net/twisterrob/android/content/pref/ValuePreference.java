package net.twisterrob.android.content.pref;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.preference.Preference;
import android.util.*;

/**
 * Dummy view-less preference so {@link android.preference.PreferenceManager#setDefaultValues} can pick it up.
 * It is suggested to be used in an {@link InvisiblePreferences} to prevent it from showing to the user.
 */
public class ValuePreference extends Preference {
	private static final Logger LOG = LoggerFactory.getLogger(ValuePreference.class);
	private int type;
	private Object value;

	public ValuePreference(Context context) {
		super(context);
	}
	public ValuePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ValuePreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public ValuePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override protected Object onGetDefaultValue(TypedArray a, int index) {
		TypedValue val = new TypedValue();
		if (a.getValue(index, val)) {
			this.type = val.type;
			switch (val.type) {
				case TypedValue.TYPE_NULL:
					return null;
				case TypedValue.TYPE_STRING:
					return val.string;
				case TypedValue.TYPE_DIMENSION:
					return val.getDimension(getContext().getResources().getDisplayMetrics());
				case TypedValue.TYPE_FRACTION:
					return val.getFraction(1.0f, 1.0f);
				case TypedValue.TYPE_FLOAT:
					return val.getFloat();
				case TypedValue.TYPE_INT_BOOLEAN:
					return val.data != 0;
				case TypedValue.TYPE_ATTRIBUTE:
				case TypedValue.TYPE_REFERENCE:
					return val.data;
				default:
					if (TypedValue.TYPE_FIRST_INT <= val.type && val.type <= TypedValue.TYPE_LAST_INT) {
						return val.data;
					} else {
						throw new IllegalArgumentException("Cannot handle type: " + val.type);
					}
			}
		}
		return super.onGetDefaultValue(a, index);
	}

	@SuppressWarnings("ConstantConditions")
	@Override protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		defaultValue = sanitizeValue(defaultValue);
		if (restorePersistedValue) {
			switch (type) {
				case TypedValue.TYPE_NULL:
					setValue(null);
					break;
				case TypedValue.TYPE_STRING:
					setValue(getPersistedString((String)defaultValue));
					break;
				case TypedValue.TYPE_DIMENSION:
				case TypedValue.TYPE_FRACTION:
				case TypedValue.TYPE_FLOAT:
					setValue(getPersistedFloat((Float)defaultValue));
					break;
				case TypedValue.TYPE_INT_BOOLEAN:
					setValue(getPersistedBoolean(Boolean.TRUE.equals(defaultValue)));
					break;
				case TypedValue.TYPE_ATTRIBUTE:
				case TypedValue.TYPE_REFERENCE:
					setValue(getPersistedInt((Integer)defaultValue));
					break;
				default:
					if (TypedValue.TYPE_FIRST_INT <= type && type <= TypedValue.TYPE_LAST_INT) {
						setValue(getPersistedInt((Integer)defaultValue));
						break;
					} else {
						throw new IllegalArgumentException("Cannot handle type: " + type);
					}
			}
		} else {
			setValue(defaultValue);
		}
	}

	private Object sanitizeValue(Object value) {
		switch (type) {
			case TypedValue.TYPE_NULL:
				return null;
			case TypedValue.TYPE_STRING:
				return value != null? value.toString() : null;
			case TypedValue.TYPE_DIMENSION:
			case TypedValue.TYPE_FRACTION:
			case TypedValue.TYPE_FLOAT:
				if (value == null) {
					value = Float.NaN;
				}
				return value;
			case TypedValue.TYPE_INT_BOOLEAN:
				return Boolean.TRUE.equals(value);
			case TypedValue.TYPE_ATTRIBUTE:
			case TypedValue.TYPE_REFERENCE:
				if (value == null || !(value instanceof Number)) {
					value = 0;
				}
				return ((Number)value).intValue();
			default:
				if (TypedValue.TYPE_FIRST_INT <= type && type <= TypedValue.TYPE_LAST_INT) {
					if (value == null || !(value instanceof Number)) {
						value = 0;
					}
					return ((Number)value).intValue();
				} else {
					throw new IllegalArgumentException("Cannot handle type: " + type);
				}
		}
	}

	private void setValue(Object value) {
		this.value = sanitizeValue(value);
		switch (type) {
			case TypedValue.TYPE_NULL:
				persistString(null);
				break;
			case TypedValue.TYPE_STRING:
				persistString((String)value);
				break;
			case TypedValue.TYPE_FRACTION:
			case TypedValue.TYPE_DIMENSION:
			case TypedValue.TYPE_FLOAT:
				persistFloat((Float)value);
				break;
			case TypedValue.TYPE_INT_BOOLEAN:
				persistBoolean((Boolean)value);
				break;
			case TypedValue.TYPE_ATTRIBUTE:
			case TypedValue.TYPE_REFERENCE:
				persistInt((Integer)value);
				break;
			default:
				if (TypedValue.TYPE_FIRST_INT <= type && type <= TypedValue.TYPE_LAST_INT) {
					persistInt((Integer)value);
					break;
				} else {
					throw new IllegalArgumentException("Cannot handle type: " + type);
				}
		}
	}
}
