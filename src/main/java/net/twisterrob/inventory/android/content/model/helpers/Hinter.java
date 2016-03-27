package net.twisterrob.inventory.android.content.model.helpers;

import java.util.*;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.text.style.*;
import android.view.View;

import static android.text.Spanned.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.java.text.Suggester.*;

public class Hinter {
	private final Context context;
	private final CategorySelectedEvent clickHandler;
	private List<CategorySuggestion<Long>> suggestions;

	public Hinter(Context context, CategorySelectedEvent clickHandler) {
		this.context = context;
		this.clickHandler = clickHandler;
	}

	public void highlight(Spannable input) {
		boolean colorMatches = App.getBPref(R.string.pref_highlightSuggestion, R.bool.pref_highlightSuggestion_default);
		if (colorMatches) {
			for (WordMatch word : CategoryDTO.getCache(context).split(input)) {
				int color = getColor(word.word().toString());
				input.setSpan(new ForegroundColorSpan(color), word.wordStart, word.wordEnd, SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
	public static void unhighlight(Spannable input) {
		ForegroundColorSpan[] fgColors = input.getSpans(0, input.length(), ForegroundColorSpan.class);
		for (ForegroundColorSpan fgColor : fgColors) {
			input.removeSpan(fgColor);
		}
	}

	public boolean hint(CharSequence userInput, boolean suggestForced, String currentTypeName) {
		boolean colorMatches = App.getBPref(R.string.pref_highlightSuggestion, R.bool.pref_highlightSuggestion_default);
		String suggestPrefVal = App.getSPref(R.string.pref_suggestCategory, R.string.pref_suggestCategory_default);
		boolean suggestAlways =
				context.getString(R.string.pref_suggestCategory_always).equals(suggestPrefVal);
		boolean suggestUnmatchedOnly =
				context.getString(R.string.pref_suggestCategory_unmatched).equals(suggestPrefVal);
		CharSequence hintText = null;
		boolean built = false;
		try {
			if (suggestAlways || suggestUnmatchedOnly || suggestForced) {
				doSuggest(userInput, suggestForced, currentTypeName);

				if (!suggestions.isEmpty()) {
					if (suggestUnmatchedOnly && !suggestForced) {
						if (isSuggested(currentTypeName)) {
							hintText = "";
						}
					}
					if (hintText == null) {
						hintText = new HintBuilder(CategoryDTO.getCache(context), suggestions, clickHandler)
								.buildHint(currentTypeName, colorMatches);
						built = true;
					}
				} else {
					if (suggestForced) {
						hintText = "Can't find any matching categories, sorry.";
					}
				}
			}
		} finally {
			suggestions = null;
		}
		clickHandler.updated(hintText);
		return built;
	}

	private boolean isSuggested(String currentTypeName) {
		CategoryCache cache = CategoryDTO.getCache(context);
		for (CategorySuggestion<Long> category : suggestions) {
			if (cache.getCategoryKey(category.getId()).equals(currentTypeName)) {
				return true;
			}
		}
		return false;
	}

	private void doSuggest(CharSequence userInput, boolean suggestForced, String currentTypeName) {
		if (!suggestForced && Category.SKIP_SUGGEST.contains(currentTypeName)) {
			suggestions = Collections.emptyList();
		} else {
			suggestions = new ArrayList<>(CategoryDTO.getCache(context).suggest(userInput));
			Collections.sort(suggestions, new Comparator<CategorySuggestion<Long>>() {
				@Override public int compare(CategorySuggestion<Long> o1, CategorySuggestion<Long> o2) {
					int m1 = o1.getMinDistance();
					int m2 = o2.getMinDistance();
					int diff = m1 < m2? -1 : (m1 == m2? 0 : 1); // Integer.compare(m1, m2)
					if (diff == 0) {
						int d1 = o1.getDistanceCount(m1);
						int d2 = o2.getDistanceCount(m2);
						diff = (d1 < d2? 1 : (d1 == d2? 0 : -1)); // -Integer.compare(d1, d2)
					}
					return diff;
				}
			});
		}
	}

	private static class HintBuilder {
		private final CategoryCache cache;
		private final CategorySelectedEvent clickHandler;
		private final List<CategorySuggestion<Long>> categorySuggestions;
		private final SpannableStringBuilder builder = new SpannableStringBuilder();
		public HintBuilder(CategoryCache cache, List<CategorySuggestion<Long>> suggestions,
				CategorySelectedEvent clickHandler) {
			this.cache = cache;
			this.categorySuggestions = suggestions;
			this.clickHandler = clickHandler;
		}

		public CharSequence buildHint(String currentTypeName, boolean colorMatches) {
			append(categorySuggestions, currentTypeName, colorMatches);
			return builder;
		}

		private void append(final List<CategorySuggestion<Long>> suggestions,
				final String currentTypeName, final boolean colorMatches) {
			CategorySuggestion<Long> last = null;
			int index = 0;
			for (CategorySuggestion<Long> categorySuggestion : suggestions) {
				if (last != null && categorySuggestion.getMinDistance() != last.getMinDistance()) {
					builder.append("\n");
					final int start = builder.length();
					builder.append("Show More...");
					final int end = builder.length();
					final List<CategorySuggestion<Long>> more = suggestions.subList(index, suggestions.size());
					builder.setSpan(new ClickableSpan() {
						@Override public void onClick(View widget) {
							builder.delete(start, end);
							append(more, currentTypeName, colorMatches); // continue appending
							clickHandler.updated(builder);
						}
					}, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
					break;
				}
				if (last != null) {
					builder.append("\n");
				}
				String categoryKey = cache.getCategoryKey(categorySuggestion.getId());
				appendSuggestion(categorySuggestion, categoryKey.equals(currentTypeName), colorMatches);
				last = categorySuggestion;
				index++;
			}
		}

		private void appendSuggestion(final CategorySuggestion<Long> categorySuggestion,
				boolean isCurrent, boolean colorMatches) {
			if (isCurrent) {
				builder.append("\u2714"); // ✔
			}
			int pathStart = builder.length();
//			builder.append(cache.getCategoryKey(categorySuggestion.getId()).replace("category_", ""));
			builder.append(cache.getCategoryPath(categorySuggestion.getId()));
			int pathEnd = builder.length();

			if (!isCurrent) {
				builder.setSpan(new ClickableSpan() {
					@Override public void onClick(View widget) {
						clickHandler.categorySelected(categorySuggestion.getId());
					}
				}, pathStart, pathEnd, SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			builder.append(" (");
			boolean first = true;
			for (KeywordSuggestion<Long> keywordSuggestion : categorySuggestion) {
				if (!first) {
					builder.append(", ");
				}
				first = false;
				int keywordStart = builder.length();
				builder.append(keywordSuggestion.getKeyword());
				for (WordSuggestion<Long> wordSuggestion : keywordSuggestion) {
					int start = keywordStart + wordSuggestion.getKeywordMatchStart();
					int end = keywordStart + wordSuggestion.getKeywordMatchEnd();
					builder.setSpan(new StyleSpan(Typeface.BOLD),
							start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				if (colorMatches) {
					ArrayList<WordSuggestion<Long>> all = new ArrayList<>();
					for (WordSuggestion<Long> wordSuggestion : keywordSuggestion) {
						all.add(wordSuggestion);
					}
					Collections.sort(all, new Comparator<WordSuggestion<Long>>() {
						@Override public int compare(WordSuggestion<Long> lhs, WordSuggestion<Long> rhs) {
							int end1 = lhs.getKeywordMatchEnd();
							int end2 = rhs.getKeywordMatchEnd();
							int increasing = end1 < end2? -1 : (end1 == end2? 0 : 1);
							return -increasing;
						}
					});
					for (WordSuggestion<Long> wordSuggestion : all) {
						String dist = String.valueOf(wordSuggestion.getDistance());
						int start = keywordStart + wordSuggestion.getKeywordMatchEnd();
						int end = start + dist.length();
						SuperscriptSpan[] spans = builder.getSpans(start, start, SuperscriptSpan.class);
						if (spans.length > 0) {
							builder.insert(start, ",");
							builder.setSpan(new SuperscriptSpan(), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
							builder.setSpan(new RelativeSizeSpan(0.6f), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						builder.insert(start, dist);
						builder.setSpan(new SuperscriptSpan(), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(new RelativeSizeSpan(0.6f), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						int color = getColor(wordSuggestion.getInputWord().toString());
						builder.setSpan(new ForegroundColorSpan(color), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}
			builder.append(")");
		}
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/31217267/253468">SO</a>
	 * @see <a href="https://gist.github.com/ro-sharp/49fd46a071a267d9e5dd">Gist</a>
	 */
	@SuppressWarnings("UnusedAssignment")
	private static int getColor(Object thing) {
		int seed = thing.hashCode();
		// Math.sin jumps big enough even when adding 1, because argument is radian and period is ~3
		int rand_r = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;
		int rand_g = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;
		int rand_b = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;

		int r = Math.round((160 + rand_r) / 2);
		int g = Math.round((160 + rand_g) / 2);
		int b = Math.round((160 + rand_b) / 2);
		return Color.argb(0xFF, r, g, b);
	}

	public interface CategorySelectedEvent {
		void categorySelected(long categoryID);
		void updated(CharSequence builder);
	}
}
