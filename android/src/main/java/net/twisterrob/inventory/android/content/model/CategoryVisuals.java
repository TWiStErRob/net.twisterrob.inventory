package net.twisterrob.inventory.android.content.model;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.ResourceTools;
import net.twisterrob.android.utils.tools.TextTools;
import net.twisterrob.inventory.android.categories.cache.CategoryCache;
import net.twisterrob.inventory.android.content.contract.ResourceNames;
import net.twisterrob.inventory.android.view.ChangeTypeDialog;

public class CategoryVisuals {

	private final @NonNull Context context;
	private final @NonNull CategoryCache cache;

	@Inject
	public CategoryVisuals(
			@ApplicationContext @NonNull Context context,
			@NonNull CategoryCache cache
	) {
		this.context = context;
		this.cache = cache;
	}

	public @Nullable CharSequence getKeywords(@NonNull String categoryName) {
		return getKeywords(categoryName, false);
	}

	public @Nullable CharSequence getKeywords(@NonNull String categoryName, boolean deep) {
		try {
			CharSequence keywords = ResourceTools.getText(context, ResourceNames.getKeywordsName(categoryName));
			if (deep) {
				SpannableStringBuilder more = new SpannableStringBuilder(keywords);
				for (String sub : cache.getChildren(categoryName)) {
					if (more.length() > 0) {
						more.append(",\n");
					}
					TextTools.appendBold(more, ResourceTools.getText(context, sub));

					CharSequence extended = getKeywordsExtended(sub);
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

	public @Nullable CharSequence getKeywordsExtended(@NonNull String categoryName) {
		SpannableStringBuilder keywords = new SpannableStringBuilder();

		CharSequence myKeywords = getKeywords(categoryName, false);
		if (myKeywords != null) {
			keywords.append(myKeywords);
		}

		Collection<String> children = cache.getChildren(categoryName);
		if (!children.isEmpty()) {
			if (0 < keywords.length()) {
				keywords.append("; ");
			}
			TextTools.appendBold(keywords, "more");
			keywords.append(": ");
			for (Iterator<String> it = children.iterator(); it.hasNext(); ) {
				TextTools.appendItalic(keywords, ResourceTools.getText(context, it.next()));
				if (it.hasNext()) {
					keywords.append(", ");
				}
			}
		}

		return keywords.length() != 0? keywords : null;
	}

	public @Nullable CharSequence getDescription(@NonNull String categoryName) {
		try {
			return ResourceTools.getText(context, ResourceNames.getDescriptionName(categoryName));
		} catch (NotFoundException ex) {
			return null;
		}
	}

	public void showKeywords(@NonNull Activity activity, long categoryID) {
		CharSequence keywords = getKeywords(cache.getCategoryKey(categoryID), true);
		ChangeTypeDialog.showKeywords(activity, cache.getCategoryPath(categoryID), keywords);
	}
}
