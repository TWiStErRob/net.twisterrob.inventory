package net.twisterrob.inventory.android.content.model;

import java.util.*;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.support.annotation.*;
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
	private final Map<String, Long> categoriesByKey = new HashMap<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final Map<String, Set<String>> categoryChildren = new TreeMap<>();
	private final LongSparseArray<CharSequence> categoryFullNames = new LongSparseArray<>();
	private final LongSparseArray<String> categoryIcons = new LongSparseArray<>();

	private final Suggester<Long> suggester = new Suggester<>(new EditAllowingIndexer<DictionaryWord<Long>>(2), 3);

	public @NonNull Collection<CategorySuggestion<Long>> suggest(@NonNull CharSequence name) {
		return suggester.suggest(name);
	}
	public @NonNull Iterable<WordMatch> split(@NonNull CharSequence name) {
		return suggester.split(name);
	}

	@WorkerThread
	public CategoryCache(@NonNull Context context) {
		Cursor cursor = App.db().listRelatedCategories(null);
		//noinspection TryFinallyCanBeTryWithResources
		try {
			while (cursor.moveToNext()) {
				addCategoryToCache(cursor, context);
			}
		} finally {
			cursor.close();
		}
	}
	private void addCategoryToCache(@NonNull Cursor cursor, @NonNull Context context) {
		String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		long categoryID = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		Long parentID = DatabaseTools.getOptionalLong(cursor, ParentColumns.PARENT_ID);
		String categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		categoriesByID.put(categoryID, categoryName);
		categoriesByKey.put(categoryName, categoryID);
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

		suggester.addText(categoryID, ResourceTools.getText(context, categoryName));
		try {
			suggester.addText(categoryID, ResourceTools.getText(context, ResourceNames.getKeywordsName(categoryName)));
		} catch (NotFoundException ex) {
			// ignore and continue
		}
	}

	private CharSequence buildFullName(@NonNull Context context, List<String> categoryPath) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (Iterator<String> it = categoryPath.iterator(); it.hasNext(); ) {
			CharSequence category = ResourceTools.getText(context, it.next());
			builder.append(category);
			if (it.hasNext()) {
				builder.append(" ▶ ");
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
	public long getId(String categoryKey) {
		return categoriesByKey.get(categoryKey);
	}
	public String getParent(String categoryKey) {
		return categoryParents.get(categoryKey);
	}
}
