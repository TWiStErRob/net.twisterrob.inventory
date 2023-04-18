package net.twisterrob.inventory.android.categories.cache;

import java.util.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.StrictMode;
import android.text.SpannableStringBuilder;

import androidx.annotation.*;
import androidx.collection.LongSparseArray;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.BaseComponent;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.java.text.*;
import net.twisterrob.java.text.Suggester.*;

public class CategoryCacheImpl implements CategoryCache {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryCache.class);
	private final LongSparseArray<String> categoriesByID = new LongSparseArray<>();
	private final Map<String, Long> categoriesByKey = new HashMap<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final Map<String, Set<String>> categoryChildren = new TreeMap<>();
	private final LongSparseArray<CharSequence> categoryFullNames = new LongSparseArray<>();
	private final LongSparseArray<String> categoryIcons = new LongSparseArray<>();

	private final Suggester<Long> suggester = new Suggester<>(new EditAllowingIndexer<DictionaryWord<Long>>(2), 3);

	@Override public @NonNull Collection<CategorySuggestion<Long>> suggest(@NonNull CharSequence name) {
		return suggester.suggest(name);
	}
	@Override public @NonNull Iterable<WordMatch> split(@NonNull CharSequence name) {
		return suggester.split(name);
	}

	@WorkerThread
	public CategoryCacheImpl(@NonNull Database database, @NonNull Context context) {
		Cursor cursor = database.listRelatedCategories(null);
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

	@Override public String getIcon(long type) {
		return categoryIcons.get(type);
	}
	@Override public Collection<String> getChildren(String name) {
		Set<String> children = categoryChildren.get(name);
		if (children != null) {
			return Collections.unmodifiableSet(children);
		} else {
			return Collections.emptySet();
		}
	}
	@Override public CharSequence getCategoryPath(long categoryID) {
		return categoryFullNames.get(categoryID);
	}
	@Override public String getCategoryKey(long categoryID) {
		return categoriesByID.get(categoryID);
	}
	@Override public long getId(String categoryKey) {
		return categoriesByKey.get(categoryKey);
	}
	@Override public String getParent(String categoryKey) {
		return categoryParents.get(categoryKey);
	}

	@SuppressLint("WrongThread")
	@AnyThread
	public static @NonNull CategoryCache getCache(@NonNull Context context) {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			// Initialization will happen only once, after that it's cached.
			return CategoryCacheInitializer.get(context);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	private static class CategoryCacheInitializer {
		/** @see #getCache(Context) */
		private static @Nullable CategoryCache CACHE;
		private static @Nullable Locale lastLocale;

		@WorkerThread
		synchronized
		public static @NonNull CategoryCache get(@NonNull Context context) {
			Locale currentLocale = AndroidTools.getLocale(context.getResources().getConfiguration());
			if (!currentLocale.equals(lastLocale)) {
				LOG.info("Locale changed from {} to {}", lastLocale, currentLocale);
				CACHE = new CategoryCacheImpl(Database.get(context), context);
				lastLocale = currentLocale;
			}
			return CACHE;
		}
	}
}
