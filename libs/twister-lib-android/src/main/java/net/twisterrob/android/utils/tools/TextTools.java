package net.twisterrob.android.utils.tools;

import android.graphics.Typeface;
import android.text.*;
import android.text.style.*;

@SuppressWarnings("unused")
public /*static*/ abstract class TextTools {
	@SuppressWarnings("UnusedReturnValue")
	public static class DescriptionBuilder {
		SpannableStringBuilder text = new SpannableStringBuilder();

		public DescriptionBuilder append(CharSequence label, Object object) {
			return append(label, object, true);
		}
		public DescriptionBuilder append(CharSequence label, Object object, boolean condition) {
			if (!condition || object == null) {
				return this;
			}
			return append(label, String.valueOf(object));
		}

		public DescriptionBuilder append(CharSequence label, CharSequence contents) {
			return append(label, contents, true);
		}
		public DescriptionBuilder append(CharSequence label, CharSequence contents, boolean condition) {
			if (!condition || contents == null) {
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
	}

	/**
	 * Returns a CharSequence that concatenates the specified array of CharSequence
	 * objects and then applies a list of zero or more tags to the entire range.
	 *
	 * @param content an array of character sequences to apply a style to
	 * @param tags the styled span objects to apply to the content
	 *        such as android.text.style.StyleSpan
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
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
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
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
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
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
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
	 */
	public static CharSequence bold(CharSequence... content) {
		return apply(content, new StyleSpan(Typeface.BOLD));
	}

	/**
	 * Returns a CharSequence that applies italics to the concatenation
	 * of the specified CharSequence objects.
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
	 */
	public static CharSequence italic(CharSequence... content) {
		return apply(content, new StyleSpan(Typeface.ITALIC));
	}

	/**
	 * Returns a CharSequence that applies a foreground color to the
	 * concatenation of the specified CharSequence objects.
	 * @see <a href="http://developer.android.com/guide/topics/resources/string-resource.html#StylingWithSpannables">String Resources > Styling with Spannables</a>
	 */
	public static CharSequence color(int color, CharSequence... content) {
		return apply(content, new ForegroundColorSpan(color));
	}

	@SuppressWarnings("UnusedReturnValue")
	public static SpannableStringBuilder appendItalic(SpannableStringBuilder builder, CharSequence content) {
		int start = builder.length();
		builder.append(content);
		int end = builder.length();
		builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return builder;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static SpannableStringBuilder appendBold(SpannableStringBuilder builder, CharSequence content) {
		int start = builder.length();
		builder.append(content);
		int end = builder.length();
		builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return builder;
	}

	protected TextTools() {
		// static utility class
	}
}
