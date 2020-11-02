package net.twisterrob.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.*;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;

import static android.view.ViewGroup.LayoutParams.*;

import net.twisterrob.android.R;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 * @see <a href="http://stackoverflow.com/a/27046784/253468">Android PreferenceActivity dialog with number picker</a>
 */
public class NumberPickerPreference extends DialogPreference {
	private Picker picker;
	private int value;
	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		picker = new NumberPickerPicker(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.NumberPickerPreference, defStyleAttr, defStyleRes);

		try {
			minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, minValue);
			maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, maxValue);
		} finally {
			a.recycle();
		}
	}

	@Override protected View onCreateDialogView() {
		return picker.createView();
	}

	@Override protected void onBindDialogView(@NonNull View view) {
		super.onBindDialogView(view);
		picker.setMinValue(minValue);
		picker.setMaxValue(maxValue);
		picker.setValue(value);
	}

	@Override protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setValue(picker.getValue());
		}
	}

	@Override protected Integer onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue? getPersistedInt(minValue) : (Integer)defaultValue);
	}

	private void setValue(int value) {
		this.value = value;
		persistInt(this.value);
	}

	interface Picker {
		View createView();
		void setMaxValue(int maxValue);
		void setMinValue(int minValue);
		int getValue();
		void setValue(int value);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private static class NumberPickerPicker implements Picker {
		private final Context context;
		private NumberPicker picker;
		public NumberPickerPicker(Context context) {
			this.context = context;
		}

		public View createView() {
			FrameLayout layout = new FrameLayout(context);

			picker = new NumberPicker(context);
			picker.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER));
			layout.addView(picker);

			return layout;
		}
		@Override public void setMaxValue(int maxValue) {
			picker.setMaxValue(maxValue);
		}
		@Override public void setMinValue(int minValue) {
			picker.setMinValue(minValue);
		}
		@Override public int getValue() {
			return picker.getValue();
		}
		@Override public void setValue(int value) {
			picker.setValue(value);
		}
	}
}
