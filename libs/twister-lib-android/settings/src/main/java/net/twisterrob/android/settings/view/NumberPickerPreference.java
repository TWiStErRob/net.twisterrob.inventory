package net.twisterrob.android.settings.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.*;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import net.twisterrob.android.settings.R;

/**
 * A {@link DialogPreference} that displays a {@link android.widget.NumberPicker} in the dialog.
 *
 * <p>This preference saves an integer value.
 * @see <a href="http://stackoverflow.com/a/27046784/253468">Android PreferenceActivity dialog with number picker</a>
 * @see androidx.preference.EditTextPreference AndroidX version is based on EditTextPreference's architecture
 */
public class NumberPickerPreference extends DialogPreference {

	private int value;

	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr,
				R.style.Preference_DialogPreference_NumberPickerPreference);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.NumberPickerPreference, defStyleAttr, defStyleRes);

		try {
			minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, minValue);
			maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, maxValue);

			if (a.getBoolean(R.styleable.NumberPickerPreference_useSimpleSummaryProvider, false)) {
				setSummaryProvider(SimpleSummaryProvider.getInstance());
			}
		} finally {
			a.recycle();
		}
	}

	@Override protected Integer onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, minValue);
	}

	@Override protected void onSetInitialValue(Object defaultValue) {
		int defaultValueInt = defaultValue != null? (int)defaultValue : minValue;
		setValue(getPersistedInt(defaultValueInt));
	}

	public int getMinValue() {
		return minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		final boolean wasBlocking = shouldDisableDependents();

		this.value = value;
		persistInt(this.value);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}

		notifyChanged();
	}

	@Override
	public boolean shouldDisableDependents() {
		return !(minValue <= value && value <= maxValue) || super.shouldDisableDependents();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.mValue = getValue();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState)state;
		super.onRestoreInstanceState(myState.getSuperState());
		setValue(myState.mValue);
	}

	private static class SavedState extends BaseSavedState {
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					@Override
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					@Override
					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};

		int mValue;

		SavedState(Parcel source) {
			super(source);
			mValue = source.readInt();
		}

		SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(mValue);
		}
	}

	/**
	 * A simple {@link androidx.preference.Preference.SummaryProvider} implementation for an
	 * {@link NumberPickerPreference}. The summary displayed will be the value set for this preference.
	 */
	public static final class SimpleSummaryProvider implements SummaryProvider<NumberPickerPreference> {

		private static SimpleSummaryProvider sSimpleSummaryProvider;

		private SimpleSummaryProvider() {
		}

		/**
		 * Retrieve a singleton instance of this simple
		 * {@link androidx.preference.Preference.SummaryProvider} implementation.
		 *
		 * @return a singleton instance of this simple
		 * {@link androidx.preference.Preference.SummaryProvider} implementation
		 */
		public static SimpleSummaryProvider getInstance() {
			if (sSimpleSummaryProvider == null) {
				sSimpleSummaryProvider = new SimpleSummaryProvider();
			}
			return sSimpleSummaryProvider;
		}

		@Override
		public CharSequence provideSummary(NumberPickerPreference preference) {
			return String.valueOf(preference.getValue());
		}
	}
}
