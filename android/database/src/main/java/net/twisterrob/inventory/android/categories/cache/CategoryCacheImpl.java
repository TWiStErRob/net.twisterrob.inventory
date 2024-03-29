package net.twisterrob.inventory.android.categories.cache;

import java.util.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.text.SpannableStringBuilder;

import androidx.annotation.*;
import androidx.collection.LongSparseArray;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.java.text.*;
import net.twisterrob.java.text.Suggester.*;

public class CategoryCacheImpl implements CategoryCache {
	private final LongSparseArray<String> categoriesByID = new LongSparseArray<>();
	private final Map<String, Long> categoriesByKey = new HashMap<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final Map<String, Set<String>> categoryChildren = new TreeMap<>();
	private final LongSparseArray<CharSequence> categoryFullNames = new LongSparseArray<>();
	private final LongSparseArray<String> categoryIcons = new LongSparseArray<>();

	private final Suggester<Long> suggester = new Suggester<>(new EditAllowingIndexer<DictionaryWord<Long>>(2), 3);

	private final @NonNull Context context;

	@Override public @NonNull Collection<CategorySuggestion<Long>> suggest(@NonNull CharSequence name) {
		return suggester.suggest(name);
	}
	@Override public @NonNull Iterable<WordMatch> split(@NonNull CharSequence name) {
		return suggester.split(name);
	}

	public CategoryCacheImpl(@ApplicationContext @NonNull Context context) {
		this.context = context;
	}

	void addCategory(@NonNull String categoryName, long categoryID, @Nullable Long parentID, String categoryIcon) {
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
		CharSequence fullName = buildFullName(categoryPath);
		categoryFullNames.put(categoryID, fullName);

		suggester.addText(categoryID, ResourceTools.getText(context, categoryName));
		try {
			suggester.addText(categoryID, ResourceTools.getText(context, ResourceNames.getKeywordsName(categoryName)));
		} catch (NotFoundException ex) {
			// ignore and continue
		}
	}

	private CharSequence buildFullName(@NonNull List<String> categoryPath) {
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

	private @NonNull List<String> getPath(@NonNull String categoryName) {
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
}
