package net.twisterrob.android.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.*;

import androidx.annotation.StyleRes;

public class TextAppearanceAccessor {
	/**
	 * {@code new TextView(context).getTextSize()} would yield the same result, but that is more intense.
	 */
	public static float getDefaultTextSize(Context context) {
		TypedArray textAppearance = obtainTextAppearance(context);
		// e.g. 14sp (= 42px on Galaxy S5 5.0.0)
		float textSize = textAppearance.getDimension(styleable.TextAppearance_textSize, 15); // see TextView
		textAppearance.recycle();
		return textSize;
	}
	public static int getDefaultTextColor(Context context) {
		TypedArray textAppearance = obtainTextAppearance(context);
		// TextView does the reading in reverse order, but in the end this is the preference order that is valid 
		ColorStateList textColor = textAppearance.getColorStateList(styleable.TextView_textColor);
		if (textColor == null) {
			textColor = textAppearance.getColorStateList(styleable.TextAppearance_textColor);
		}
		textAppearance.recycle();
		if (textColor == null) {
			textColor = ColorStateList.valueOf(0xFF000000); // see TextView
		}
		return textColor.getDefaultColor();
	}

	private static TypedArray obtainTextAppearance(Context context) {
		TypedArray theme = context.obtainStyledAttributes(styleable.Theme);
		// e.g. 0x010302cb {@android:style/Widget.Material.Light.TextView}
		@StyleRes int textViewStyle = theme.getResourceId(styleable.Theme_textViewStyle, 0);
		theme.recycle();
		TypedArray textView = context.obtainStyledAttributes(textViewStyle, styleable.TextView);
		// e.g. 0x01030206 {@android:style/TextAppearance.Material.Small}
		@StyleRes int textAppearanceStyle = textView.getResourceId(styleable.TextView_textAppearance, 0);
		textView.recycle();
		return context.obtainStyledAttributes(textAppearanceStyle, styleable.TextAppearance);
	}

	private static int[] getIntArrayField(Class<?> clazz, String fieldName) {
		try {
			return (int[])clazz.getField(fieldName).get(null);
		} catch (NoSuchFieldException ex) {
			return null;
		} catch (IllegalAccessException ex) {
			return null;
		}
	}
	private static int getIntField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getField(fieldName).getInt(null);
		} catch (NoSuchFieldException ex) {
			return 0;
		} catch (IllegalAccessException ex) {
			return 0;
		}
	}

	@SuppressLint("PrivateApi")
	static class styleable {
		private static final int[] Theme;
		private static final int Theme_textViewStyle;
		private static final int[] TextView;
		private static final int TextView_textAppearance;
		private static final int TextView_textColor;
		private static final int[] TextAppearance;
		private static final int TextAppearance_textSize;
		private static final int TextAppearance_textColor;

		static {
			Class<?> styleable = null;
			try {
				styleable = Class.forName("com.android.internal.R$styleable", false, android.R.class.getClassLoader());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Theme = getIntArrayField(styleable, "Theme");
			Theme_textViewStyle = getIntField(styleable, "Theme_textViewStyle");
			TextView = getIntArrayField(styleable, "TextView");
			TextView_textAppearance = getIntField(styleable, "TextView_textAppearance");
			TextView_textColor = getIntField(styleable, "TextView_textColor");
			TextAppearance = getIntArrayField(styleable, "TextAppearance");
			TextAppearance_textSize = getIntField(styleable, "TextAppearance_textSize");
			TextAppearance_textColor = getIntField(styleable, "TextAppearance_textColor");
		}
	}
}
