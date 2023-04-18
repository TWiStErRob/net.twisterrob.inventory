package net.twisterrob.inventory.android.categories.cache;

import java.util.Collection;

import androidx.annotation.NonNull;

import net.twisterrob.java.text.Suggester;

public interface CategoryCache {
	@NonNull Collection<Suggester.CategorySuggestion<Long>> suggest(@NonNull CharSequence name);
	@NonNull Iterable<Suggester.WordMatch> split(@NonNull CharSequence name);
	String getIcon(long type);
	Collection<String> getChildren(String name);
	CharSequence getCategoryPath(long categoryID);
	String getCategoryKey(long categoryID);
	long getId(String categoryKey);
	String getParent(String categoryKey);
}
