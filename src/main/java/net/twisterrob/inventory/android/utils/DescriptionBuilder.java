package net.twisterrob.inventory.android.utils;

import android.graphics.Typeface;
import android.text.*;
import android.text.style.*;

public class DescriptionBuilder {
	SpannableStringBuilder text = new SpannableStringBuilder();

	public DescriptionBuilder append(CharSequence label, Object object) {
		if (object == null) {
			return this;
		}
		return append(label, String.valueOf(object));
	}
	public DescriptionBuilder append(CharSequence label, CharSequence contents) {
		if (contents == null) {
			return this;
		}
		if (text.length() > 0) {
			newLine();
		}
		text.append(bold(label));
		text.append(": ");
		text.append(contents);
		return this;
	}
	public DescriptionBuilder newLine() {
		text.append("\n");
		return this;
	}

	public CharSequence build() {
		return text;
	}

	/**
	 * Returns a CharSequence that concatenates the specified array of CharSequence
	 * objects and then applies a list of zero or more tags to the entire range.
	 *
	 * @param content an array of character sequences to apply a style to
	 * @param tags the styled span objects to apply to the content
	 *        such as android.text.style.StyleSpan
	 *
	 */
	private static CharSequence apply(CharSequence[] content, Object... tags) {
		SpannableStringBuilder text = new SpannableStringBuilder();
		openTags(text, tags);
		for (CharSequence item : content) {
			text.append(item);
		}
		closeTags(text, tags);
		return text;
	}

	/**
	 * Iterates over an array of tags and applies them to the beginning of the specified
	 * Spannable object so that future text appended to the text will have the styling
	 * applied to it. Do not call this method directly.
	 */
	private static void openTags(Spannable text, Object... tags) {
		for (Object tag : tags) {
			text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
		}
	}

	/**
	 * "Closes" the specified tags on a Spannable by updating the spans to be
	 * endpoint-exclusive so that future text appended to the end will not take
	 * on the same styling. Do not call this method directly.
	 */
	private static void closeTags(Spannable text, Object... tags) {
		int len = text.length();
		for (Object tag : tags) {
			if (len > 0) {
				text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				text.removeSpan(tag);
			}
		}
	}
	/**
	 * Returns a CharSequence that applies boldface to the concatenation
	 * of the specified CharSequence objects.
	 */
	public static CharSequence bold(CharSequence... content) {
		return apply(content, new StyleSpan(Typeface.BOLD));
	}

	/**
	 * Returns a CharSequence that applies italics to the concatenation
	 * of the specified CharSequence objects.
	 */
	public static CharSequence italic(CharSequence... content) {
		return apply(content, new StyleSpan(Typeface.ITALIC));
	}

	/**
	 * Returns a CharSequence that applies a foreground color to the
	 * concatenation of the specified CharSequence objects.
	 */
	public static CharSequence color(int color, CharSequence... content) {
		return apply(content, new ForegroundColorSpan(color));
	}
}