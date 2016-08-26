package net.twisterrob.android.utils.tools;

import java.util.concurrent.atomic.AtomicReference;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.Build.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.rarepebble.colorpicker.ColorPickerView;

public class DialogTools {
	@UiThread
	public static AlertDialog.Builder prompt(final @NonNull Context context,
			@Nullable String initialValue, final @NonNull PopupCallbacks<String> callbacks) {
		final EditText input = new EditText(context);
		input.setSingleLine(true);
		input.setText(initialValue);
		AndroidTools.showKeyboard(input);

		final AtomicReference<Dialog> dialog = new AtomicReference<>();
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = input.getText().toString();
					callbacks.finished(value);
					dialog.get().dismiss();
				}
				return false;
			}
		});
		return new AlertDialog.Builder(context) {
			@Override public @NonNull AlertDialog create() {
				AlertDialog createdDialog = super.create();
				if (null != dialog.getAndSet(createdDialog)) { // steal created dialog
					throw new UnsupportedOperationException("Cannot create multiple dialogs from this builder.");
				}
				return createdDialog;
			}
		}
				.setView(input)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						callbacks.finished(value);
					}
				})
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						callbacks.finished(null);
					}
				});
	}
	public static AlertDialog.Builder confirm(@NonNull Context context,
			final @NonNull PopupCallbacks<Boolean> callbacks) {
		return new DefaultBuilder(context)
				.setPositiveButton(android.R.string.yes, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						callbacks.finished(true);
					}
				})
				.setNegativeButton(android.R.string.no, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						callbacks.finished(false);
					}
				})
				.setCancelable(true)
				.setOnCancelListener(new OnCancelListener() {
					@Override public void onCancel(DialogInterface dialog) {
						callbacks.finished(null);
					}
				});
	}
	public static AlertDialog.Builder notify(@NonNull Context context,
			final @NonNull PopupCallbacks<Boolean> callbacks) {
		return new DefaultBuilder(context)
				.setNeutralButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						callbacks.finished(true);
					}
				})
				.setCancelable(true)
				.setOnCancelListener(new OnCancelListener() {
					@Override public void onCancel(DialogInterface dialog) {
						callbacks.finished(null);
					}
				});
	}
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static AlertDialog.Builder pickNumber(@NonNull Context context,
			@IntRange(from = 0) int initial, @IntRange(from = 0) Integer min, @IntRange(from = 0) Integer max,
			final @NonNull PopupCallbacks<Integer> callbacks) {
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			final NumberPicker picker = new NumberPicker(context);
			if (min != null) {
				picker.setMinValue(min);
			}
			if (max != null) {
				picker.setMaxValue(max);
			}
			picker.setValue(initial);
			return new AlertDialog.Builder(context)
					.setView(picker)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							callbacks.finished(picker.getValue());
						}
					})
					.setNegativeButton(android.R.string.cancel, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							callbacks.finished(null);
						}
					})
					.setTitle("Pick a number");
		} else {
			return prompt(context, Integer.toString(initial), new PopupCallbacks<String>() {
				@Override public void finished(String value) {
					try {
						callbacks.finished(Integer.parseInt(value));
					} catch (NumberFormatException ex) {
						callbacks.finished(null);
					}
				}
			})
					.setTitle("Pick a number");
		}
	}
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static AlertDialog.Builder pickColor(@NonNull Context context,
			@ColorInt int initial, final @NonNull PopupCallbacks<Integer> callbacks) {
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			final ColorPickerView picker = new ColorPickerView(context);
			picker.setColor(initial);
			return new AlertDialog.Builder(context)
					.setView(picker)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							callbacks.finished(picker.getColor());
						}
					})
					.setNegativeButton(android.R.string.cancel, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							callbacks.finished(null);
						}
					})
					.setTitle("Pick a color");
		} else {
			return prompt(context, Integer.toHexString(initial), new PopupCallbacks<String>() {
				@Override public void finished(String value) {
					try {
						callbacks.finished(Integer.parseInt(value, 16));
					} catch (NumberFormatException ex) {
						callbacks.finished(null);
					}
				}
			})
					.setTitle("Pick a color");
		}
	}
	@UiThread
	public interface PopupCallbacks<T> {
		void finished(T value);
		/**
		 * @see DoNothing#instance()
		 */
		PopupCallbacks<?> NO_CALLBACK = new DoNothing();

		class DoNothing implements PopupCallbacks<Object> {
			@Override public void finished(Object value) {

			}
			@SuppressWarnings("unchecked")
			public static <T> PopupCallbacks<T> instance() {
				return (PopupCallbacks<T>)NO_CALLBACK;
			}
		}
	}

	private static class DefaultBuilder extends AlertDialog.Builder {
		public DefaultBuilder(Context context) {
			super(context);
		}

		@Override public AlertDialog create() {
			final AlertDialog dialog = super.create();
			dialog.setCanceledOnTouchOutside(true);
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override public void run() {
					// TODO is this available earlier somehow?
					View message = dialog.findViewById(android.R.id.message);
					if (message instanceof TextView) {
						((TextView)message).setMovementMethod(LinkMovementMethod.getInstance());
					}
				}
			});
			return dialog;
		}
	}
}
