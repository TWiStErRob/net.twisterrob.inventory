package net.twisterrob.inventory.android.content.model;

import java.util.*;

import org.slf4j.*;

import android.content.*;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.*;
import android.text.SpannableStringBuilder;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.view.ChangeTypeDialog;

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
	@Override public CharSequence getShareDescription(Context context) {
		return null;
	}
	@Override public Intent createShareIntent(Context context) {
		return null;
	}
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Category #%1$d: '%2$s' in %3$s", id, name, parentID);
	}

	public static @Nullable CharSequence getKeywords(@NonNull Context context, @NonNull String categoryName) {
		return getKeywords(context, categoryName, false);
	}
	public static @Nullable CharSequence getKeywords(@NonNull Context context, @NonNull String categoryName,
			boolean deep) {
		try {
			CharSequence keywords = AndroidTools.getText(context, categoryName + "_keywords");
			if (deep) {
				SpannableStringBuilder more = new SpannableStringBuilder(keywords);
				for (String sub : getCache(context).getChildren(categoryName)) {
					if (more.length() > 0) {
						more.append(",\n");
					}
					TextTools.appendBold(more, AndroidTools.getText(context, sub));

					CharSequence extended = getKeywordsExtended(context, sub);
					if (extended != null) {
						more.append(" (").append(extended).append(")");
					}
				}
				keywords = more;
			}
			return keywords.length() != 0? keywords : null;
		} catch (NotFoundException ex) {
			return null;
		}
	}

	public static @Nullable CharSequence getKeywordsExtended(@NonNull Context context, @NonNull String categoryName) {
		SpannableStringBuilder keywords = new SpannableStringBuilder();

		CharSequence myKeywords = getKeywords(context, categoryName, false);
		if (myKeywords != null) {
			keywords.append(myKeywords);
		}

		Collection<String> children = getCache(context).getChildren(categoryName);
		if (!children.isEmpty()) {
			if (0 < keywords.length()) {
				keywords.append("; ");
			}
			TextTools.appendBold(keywords, "more");
			keywords.append(": ");
			for (Iterator<String> it = children.iterator(); it.hasNext(); ) {
				TextTools.appendItalic(keywords, AndroidTools.getText(context, it.next()));
				if (it.hasNext()) {
					keywords.append(", ");
				}
			}
		}

		return keywords.length() != 0? keywords : null;
	}

	public static @Nullable CharSequence getDescription(@NonNull Context context, @NonNull String categoryName) {
		try {
			return AndroidTools.getText(context, categoryName + "_description");
		} catch (NotFoundException ex) {
			return null;
		}
	}

	public static void showKeywords(@NonNull Context context, long categoryID) {
		CategoryCache cache = CategoryDTO.getCache(context);
		CharSequence keywords = CategoryDTO.getKeywords(context, cache.getCategoryKey(categoryID), true);
		ChangeTypeDialog.showKeywords(context, cache.getCategoryPath(categoryID), keywords);
	}

	public static @NonNull CategoryCache getCache(Context context) {
		return CategoryCacheInitializer.get(context);
	}

	private static class CategoryCacheInitializer {
		private static final Logger LOG = LoggerFactory.getLogger(CategoryCacheInitializer.class);
		/** @see #getCache(Context) */
		private static CategoryCache CACHE;
		private static Locale lastLocale;

		synchronized
		public static CategoryCache get(Context context) {
			Locale currentLocale = AndroidTools.getLocale(context.getResources().getConfiguration());
			if (!currentLocale.equals(lastLocale)) {
				LOG.info("Locale changed from {} to {}", lastLocale, currentLocale);
				CACHE = new CategoryCache(context);
				lastLocale = currentLocale;
			}
			return CACHE;
		}
	}
}
