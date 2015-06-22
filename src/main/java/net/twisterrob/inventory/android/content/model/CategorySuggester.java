package net.twisterrob.inventory.android.content.model;

import java.util.*;
import java.util.regex.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.text.SpannableStringBuilder;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public class CategorySuggester {
	private static final Pattern WORD_SEARCH = Pattern.compile("(\\p{L}\\p{M}*+){3,}");
	private static final Pattern KEYWORD_SPLITTER = Pattern.compile("[,;/()]+");
	/** word -> { (category, keyword) } */
	private final Map<String, Collection<IndexEntry>> index = new HashMap<>();
	private final LongSparseArray<String> categoriesByID = new LongSparseArray<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final LongSparseArray<CharSequence> categoryFullNames = new LongSparseArray<>();
	private final LongSparseArray<String> categoryIcons = new LongSparseArray<>();
	private Locale lastLocale;

	CategorySuggester() {
		// limit visibility
	}

	public @NonNull Collection<Suggestion> suggest(@NonNull CharSequence name) {
		List<Suggestion> suggestions = new ArrayList<>();

		Matcher m = WORD_SEARCH.matcher(name);
		while (m.find()) {
			String word = m.group().toLowerCase();
			Collection<IndexEntry> categories = index.get(word);
			if (categories != null) {
				for (IndexEntry index : categories) {
					Suggestion suggestion = new Suggestion();
					suggestion.input = name;
					suggestion.search = word;
					suggestion.match = index.keyword;
					suggestion.matchStart = index.wordStart;
					suggestion.matchEnd = index.wordEnd;
					suggestion.category = index.category;
					suggestions.add(suggestion);
				}
			}
		}

		return suggestions;
	}

	void init(@NonNull Context context) {
		Locale currentLocale = context.getResources().getConfiguration().locale;
		if (currentLocale.equals(lastLocale)) {
			return;
		}
		lastLocale = currentLocale;

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
			}
			List<String> categoryPath = getPath(categoryName);
			CharSequence fullName = buildFullName(context, categoryPath);
			categoryFullNames.put(categoryID, fullName);

			addToIndex(categoryID, AndroidTools.getText(context, categoryName));
			try {
				addToIndex(categoryID, AndroidTools.getText(context, categoryName + "_keywords"));
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

	private void addToIndex(long categoryID, CharSequence categoryText) {
		String[] keywords = KEYWORD_SPLITTER.split(categoryText);
		for (String keyword : keywords) {
			keyword = keyword.trim();
			Matcher m = WORD_SEARCH.matcher(keyword);
			while (m.find()) {
				String word = m.group().toLowerCase();
				Collection<IndexEntry> wordSuggestions = index.get(word);
				if (wordSuggestions == null) {
					wordSuggestions = new ArrayList<>();
					index.put(word, wordSuggestions);
				}
				IndexEntry index = new IndexEntry();
				index.word = word;
				index.keyword = keyword;
				index.wordStart = m.start();
				index.wordEnd = m.end();
				index.category = categoryID;
				wordSuggestions.add(index);
			}
		}
	}
	public String getIcon(long type) {
		return categoryIcons.get(type);
	}

	private static class IndexEntry {
		long category;
		String keyword;
		String word;
		int wordStart;
		int wordEnd;
	}

	public class Suggestion {
		/** Suggestion created for this input. */
		public CharSequence input;
		/** Matched part of index when looking for {@code search}. */
		public CharSequence match;
		public int matchStart;
		public int matchEnd;
		/** Part of input that was searched in index. */
		public CharSequence search;
		public long category;

		public CharSequence getCategoryPath() {
			return categoryFullNames.get(category);
		}
		public String getCategoryKey() {
			return categoriesByID.get(category);
		}
	}
}
