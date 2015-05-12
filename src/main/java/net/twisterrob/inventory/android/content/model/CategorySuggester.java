package net.twisterrob.inventory.android.content.model;

import java.util.*;
import java.util.regex.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public class CategorySuggester {
	private static final Pattern WORD_SPLITTER = Pattern.compile("(\\p{L}\\p{M}*+){3,}");
	private final Map<String, Set<String>> index = new HashMap<>();
	private final Map<Long, String> categoriesByID = new TreeMap<>();
	private final Map<String, Long> categoriesByName = new TreeMap<>();
	private final Map<String, String> categoryParents = new TreeMap<>();
	private final Map<String, CharSequence> categoryFullNames = new TreeMap<>();
	private Locale lastLocale;

	CategorySuggester() {
		// limit visibility
	}

	public @NonNull Collection<String> suggest(@NonNull CharSequence name) {
		Set<String> suggestions = new HashSet<>();

		Matcher m = WORD_SPLITTER.matcher(name);
		while (m.find()) {
			String word = m.group().toLowerCase();
			Set<String> categories = index.get(word);
			if (categories != null) {
				suggestions.addAll(categories);
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

		Cursor cursor = App.db().listItemCategories();
		while (cursor.moveToNext()) {
			String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
			long categoryID = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
			Long parentID = DatabaseTools.getOptionalLong(cursor, ParentColumns.PARENT_ID);
			categoriesByID.put(categoryID, categoryName);
			categoriesByName.put(categoryName, categoryID);
			if (parentID != null) {
				String parentName = categoriesByID.get(parentID);
				assert parentName != null : "Missing parent. Is the cursor returning them in parent-first order?";
				categoryParents.put(categoryName, parentName);
			}
			List<String> categoryPath = getPath(categoryName);
			CharSequence fullName = buildFullName(context, categoryPath);
			categoryFullNames.put(categoryName, fullName);

			addToIndex(categoryName, AndroidTools.getText(context, categoryName));
			try {
				addToIndex(categoryName, AndroidTools.getText(context, categoryName + "_keywords"));
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

	private void addToIndex(String categoryName, CharSequence categoryText) {
		Matcher m = WORD_SPLITTER.matcher(categoryText);
		while (m.find()) {
			String word = m.group().toLowerCase();
			Set<String> wordSuggestions = index.get(word);
			if (wordSuggestions == null) {
				wordSuggestions = new HashSet<>();
				index.put(word, wordSuggestions);
			}
			wordSuggestions.add(categoryName);
		}
	}

	public CharSequence getFullName(String categoryName) {
		return categoryFullNames.get(categoryName);
	}
	public long getCategoryID(String categoryName) {
		return categoriesByName.get(categoryName);
	}
}
