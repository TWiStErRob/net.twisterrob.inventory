package net.twisterrob.inventory.android.content.model;

import java.util.Locale;
import java.util.regex.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.*;
import android.text.*;
import android.text.style.ForegroundColorSpan;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.inventory.android.content.contract.Category;

public class CategoryDTO extends ImagedDTO {
	private static final Uri APP_RESOURCE_RAW = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/raw/");
	private static final CategorySuggester SUGGESTER = new CategorySuggester();

	public Long parentID;
	public String parentName;

	public static CategoryDTO fromCursor(Cursor cursor) {
		CategoryDTO category = new CategoryDTO();
		return category.fromCursorInternal(cursor);
	}

	@Override
	protected CategoryDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseTools.getOptionalLong(cursor, Category.PARENT_ID);
		parentName = DatabaseTools.getOptionalString(cursor, Category.PARENT_NAME);

		return this;
	}
	@Override public Uri getImageUri() {
		return APP_RESOURCE_RAW.buildUpon().appendPath(typeImage).build();
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Category #%1$d: '%2$s' in %3$s", id, name, parentID);
	}

	public static @Nullable CharSequence getShortKeywords(@NonNull Context context, @NonNull String categoryName) {
		try {
			CharSequence keywords = AndroidTools.getText(context, categoryName + "_keywords");
			return keywords.toString().replaceAll("\\s*\\(.*?\\)", ""); // remove stuff in parentheses
		} catch (NotFoundException ex) {
			return null;
		}
	}
	public static @Nullable CharSequence getKeywords(@NonNull Context context, @NonNull String categoryName) {
		try {
			CharSequence text = AndroidTools.getText(context, categoryName + "_keywords");
			SpannableStringBuilder builder = new SpannableStringBuilder(text);
			Pattern p = Pattern.compile("\\s*\\(.*?\\)|\\s*[,;]\\s*"); // match parentheses and list separators
			Matcher m = p.matcher(builder);

			// inverse of appendReplacement and appendTail, treat everything that's not matched
			//int start = m.regionStart();
			while (m.find()) {
				builder.setSpan(new ForegroundColorSpan(Color.LTGRAY),
						m.start(), m.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				//builder.setSpan(new StyleSpan(Typeface.BOLD), start, m.start(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				//start = m.end();
			}
			//builder.setSpan(new StyleSpan(Typeface.BOLD), start, m.regionEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

			return builder;
		} catch (NotFoundException ex) {
			return null;
		}
	}

	public static @Nullable CharSequence getDescription(@NonNull Context context, @NonNull String categoryName) {
		try {
			return AndroidTools.getText(context, categoryName + "_description");
		} catch (NotFoundException ex) {
			return null;
		}
	}

	public static CategorySuggester getSuggester(Context context) {
		SUGGESTER.init(context);
		return SUGGESTER;
	}
}
