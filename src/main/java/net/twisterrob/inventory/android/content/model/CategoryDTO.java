package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.inventory.android.content.contract.Category;

public class CategoryDTO extends ImagedDTO {
	private static final Uri APP_RESOURCE_RAW = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/raw/");

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

	public static CharSequence getShortKeywords(Context context, String categoryName) {
		CharSequence keywords = getKeywords(context, categoryName);
		if (keywords == null) {
			return null;
		}
		return keywords.toString().replaceAll("\\s*\\(.*?\\)", "");
	}
	public static CharSequence getKeywords(Context context, String categoryName) {
		try {
			return AndroidTools.getText(context, categoryName + "_keywords");
		} catch (NotFoundException ex) {
			return null;
		}
	}
}
