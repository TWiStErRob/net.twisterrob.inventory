package net.twisterrob.android.wiring;

import java.util.Locale;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import net.twisterrob.android.R;
import net.twisterrob.android.utils.tools.AndroidTools.*;
import net.twisterrob.android.view.RepeatListener;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

// TODO merge with NumberPickerPreference
public class NumberPickerWidget {
	private static final Formatter FORMATTER = new Formatter();
	private float value;
	private float minValue;
	private float maxValue;
	private final TextView display;
	private OnValueChangeListener onValueChangeListener;

	/**
	 * Interface to listen for changes of the current value.
	 * @see android.widget.NumberPicker.OnValueChangeListener
	 */
	public interface OnValueChangeListener {

		/**
		 * Called upon a change of the current value.
		 *
		 * @param picker The NumberPicker associated with this listener.
		 * @param oldVal The previous value.
		 * @param newVal The new value.
		 */
		void onValueChange(NumberPickerWidget picker, float oldVal, float newVal);
	}

	public NumberPickerWidget(View view) {
		View decrease = view.findViewById(R.id.widget_number_decrease);
		decrease.setOnTouchListener(new RepeatListener());
		decrease.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setValueInternal(value - 1, true);
			}
		});
		View increase = view.findViewById(R.id.widget_number_increase);
		increase.setOnTouchListener(new RepeatListener());
		increase.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setValueInternal(value + 1, true);
			}
		});
		display = (TextView)view.findViewById(R.id.widget_number_display);
		display.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				pickNumber(v.getContext(), (int)value, (int)minValue, (int)maxValue, new PopupCallbacks<Integer>() {
					@Override public void finished(Integer value) {
						if (value != null) {
							setValueInternal(value, true);
						}
					}
				}).show();
			}
		});
	}
	private void updateDisplay() {
		display.setText(FORMATTER.format(value));
	}

	/**
	 * Sets the listener to be notified on change of the current value.
	 *
	 * @param onValueChangedListener The listener.
	 */
	public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
		this.onValueChangeListener = onValueChangedListener;
	}

	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		setValueInternal(value, false);
	}
	private void setValueInternal(float value, boolean notify) {
		value = Math.max(value, minValue);
		value = Math.min(value, maxValue);
		if (this.value == value) {
			return;
		}
		this.value = value;
		updateDisplay();
		if (notify && onValueChangeListener != null) {
			onValueChangeListener.onValueChange(this, this.value, value);
		}
	}
	public float getMinValue() {
		return minValue;
	}
	public void setMinValue(float minValue) {
		if (this.minValue == minValue) {
			return;
		}
		this.minValue = minValue;
		this.minValue = minValue;
		if (this.value < this.minValue) {
			setValue(this.minValue);
		}
	}
	public float getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(float maxValue) {
		if (this.maxValue == maxValue) {
			return;
		}
		this.maxValue = maxValue;
		if (this.maxValue < this.value) {
			setValue(this.maxValue);
		}
	}

	private static class Formatter {
		private final StringBuilder cachedBuilder = new StringBuilder();
		private final Object[] cachedArgs = new Object[1];
		private Locale locale;
		java.util.Formatter formatter;

		Formatter() {
			init(Locale.getDefault());
		}

		private void init(Locale locale) {
			this.formatter = new java.util.Formatter(cachedBuilder, locale);
			this.locale = locale;
		}

		public String format(float value) {
			final Locale currentLocale = Locale.getDefault();
			if (this.locale != currentLocale) {
				init(currentLocale);
			}
			cachedBuilder.delete(0, cachedBuilder.length());
			cachedArgs[0] = (int)value;
			//noinspection MalformedFormatString IDEA can't see because of cached args
			formatter.format("%d", cachedArgs);
			return formatter.toString();
		}
	}
}
