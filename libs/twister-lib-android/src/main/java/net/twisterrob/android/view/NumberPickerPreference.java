package net.twisterrob.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.*;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import static android.view.ViewGroup.LayoutParams.*;

import net.twisterrob.android.R;
import net.twisterrob.android.utils.tools.AndroidTools;

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
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			picker = new EditTextPicker(context);
		} else {
			picker = new NumberPickerPicker(context);
		}

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

	private static class EditTextPicker extends TextWatcherAdapter implements Picker {
		private final Context context;
		private int value;
		private int minValue = Integer.MIN_VALUE;
		private int maxValue = Integer.MAX_VALUE;
		private EditText editor;
		private TextView message;
		public EditTextPicker(Context context) {
			this.context = context;
		}

		@Override public View createView() {
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);

			message = new TextView(context);
			LayoutParams messageParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
			messageParams.gravity = Gravity.CENTER_HORIZONTAL;
			messageParams.topMargin = AndroidTools.dipInt(context, 4);
			message.setLayoutParams(messageParams);
			updateMessage();
			layout.addView(message);

			editor = new EditText(context);
			LayoutParams editorParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
			editorParams.gravity = Gravity.CENTER_HORIZONTAL;
			editor.setLayoutParams(editorParams);
			editor.setSingleLine();
			editor.setGravity(Gravity.CENTER_HORIZONTAL);
			editor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
			AndroidTools.showKeyboard(editor);
			editor.addTextChangedListener(this);
			layout.addView(editor);

			return layout;
		}
		private void updateMessage() {
			if (message != null) {
				message.setText(context.getString(R.string.pref_number_picker_invalid_input, minValue, maxValue));
			}
		}

		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				long number = Long.parseLong(s.toString());
				if (number < minValue) {
					throw new IllegalArgumentException(
							"Number " + number + " is too low.");
				}
				if (maxValue < number) {
					throw new IllegalArgumentException(
							"Number " + number + " is too high.");
				}
				value = (int)number;
				editor.setError(null);
			} catch (NumberFormatException ex) {
				if (s.length() == 0) {
					editor.setError("Please enter a number.");
				} else {
					editor.setError("Invalid number: " + s);
				}
			} catch (IllegalArgumentException ex) {
				editor.setError(ex.getMessage());
			}
		}

		@Override public void setMinValue(int minValue) {
			this.minValue = minValue;
			if (value < minValue) {
				value = minValue;
			}
			updateEms();
			updateMessage();
		}
		@Override public void setMaxValue(int maxValue) {
			this.maxValue = maxValue;
			if (maxValue < value) {
				value = maxValue;
			}
			updateEms();
			updateMessage();
		}
		private void updateEms() {
			if (editor != null) {
				editor.setEms(1 + (int)Math.max(Math.log10(minValue), Math.log10(maxValue)) + 2); // sign + value + icon
			}
		}

		@Override public int getValue() {
			return value;
		}
		@Override public void setValue(int value) {
			if (value < minValue) {
				value = minValue;
			}
			if (maxValue < value) {
				value = maxValue;
			}
			this.value = value;
			if (editor != null) {
				editor.setText(String.valueOf(value));
				editor.setSelection(editor.getText().length());
			}
		}
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
