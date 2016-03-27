package net.twisterrob.inventory.android.content.model;

import java.util.*;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.text.SpannableStringBuilder;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.java.text.*;
import net.twisterrob.java.text.Suggester.*;

public class CategoryCache {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryCache.class);
	private final LongSparseArray<String> categoriesByID = new LongSparseArray<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final Map<String, Set<String>> categoryChildren = new TreeMap<>();
	private final LongSparseArray<CharSequence> categoryFullNames = new LongSparseArray<>();
	private final LongSparseArray<String> categoryIcons = new LongSparseArray<>();
	private Locale lastLocale;

	private final Suggester<Long> suggester = new Suggester<>(new EditAllowingIndexer<DictionaryWord<Long>>(2), 3);

	CategoryCache() {
		// limit visibility
	}

	public @NonNull Collection<CategorySuggestion<Long>> suggest(@NonNull CharSequence name) {
		return suggester.suggest(name);
	}
	public @NonNull Iterable<WordMatch> split(@NonNull CharSequence name) {
		return suggester.split(name);
	}

	synchronized void init(@NonNull Context context) {
		Locale currentLocale = context.getResources().getConfiguration().locale;
		if (currentLocale.equals(lastLocale)) {
			return;
		}
		LOG.warn("Locale changed from {} to {}", lastLocale, currentLocale);
		lastLocale = currentLocale;
		doInit(context);
	}

	private void doInit(@NonNull Context context) {
		Cursor cursor = App.db().listRelatedCategories(null);
		while (cursor.moveToNext()) {
			String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
			long categoryID = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
			Long parentID = DatabaseTools.getOptionalLong(cursor, ParentColumns.PARENT_ID);
			String categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
			categoriesByID.put(categoryID, categoryName);
			categoryIcons.put(categoryID, categoryIcon);
			if (parentID != null) {
				String parentName = categoriesByID.get(parentID);
				assert parentName != null : "Missing parent. Is the cursor returning them in parent-first order?";
				categoryParents.put(categoryName, parentName);
				Set<String> children = categoryChildren.get(parentName);
				if (children == null) {
					children = new TreeSet<>();
					categoryChildren.put(parentName, children);
				}
				children.add(categoryName);
			}
			List<String> categoryPath = getPath(categoryName);
			CharSequence fullName = buildFullName(context, categoryPath);
			categoryFullNames.put(categoryID, fullName);

			suggester.addText(categoryID, AndroidTools.getText(context, categoryName));
			try {
				suggester.addText(categoryID, AndroidTools.getText(context, categoryName + "_keywords"));
			} catch (NotFoundException ex) {
				// ignore and continue
			}
		}
		cursor.close();
	}

	private CharSequence buildFullName(@NonNull Context context, List<String> categoryPath) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (Iterator<String> it = categoryPath.iterator(); it.hasNext(); ) {
			CharSequence category = AndroidTools.getText(context, it.next());
			builder.append(category);
			if (it.hasNext()) {
				builder.append(" > ");
			}
		}
		return builder;
	}

	private List<String> getPath(@NonNull String categoryName) {
		LinkedList<String> cats = new LinkedList<>();
		String parentCat = categoryName;
		do {
			cats.addFirst(parentCat);
		} while ((parentCat = categoryParents.get(parentCat)) != null);
		return cats;
	}

	public String getIcon(long type) {
		return categoryIcons.get(type);
	}
	public Collection<String> getChildren(String name) {
		Set<String> children = categoryChildren.get(name);
		if (children != null) {
			return Collections.unmodifiableSet(children);
		} else {
			return Collections.emptySet();
		}
	}
	public CharSequence getCategoryPath(long categoryID) {
		return categoryFullNames.get(categoryID);
	}
	public String getCategoryKey(long categoryID) {
		return categoriesByID.get(categoryID);
	}
}
