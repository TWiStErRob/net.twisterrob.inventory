package net.twisterrob.inventory.android.content.model.helpers;

import java.util.*;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import static android.text.Spanned.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.java.text.Suggester.*;

public class Hinter {
	private final Context context;
	private final HintBuilder adapter;

	public Hinter(Context context, CategorySelectedEvent clickHandler) {
		this.context = context;
		this.adapter = new HintBuilder(context, null, clickHandler);
	}

	public void highlight(Spannable input) {
		boolean colorMatches = App.getBPref(R.string.pref_highlightSuggestion, R.bool.pref_highlightSuggestion_default);
		if (colorMatches) {
			for (WordMatch word : CategoryDTO.getCache(context).split(input)) {
				int color = PictureHelper.getColor(word.word().toString());
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
		adapter.setShowEditDistances(colorMatches);
		String suggestPrefVal = App.getSPref(R.string.pref_suggestCategory, R.string.pref_suggestCategory_default);
		boolean suggestAlways =
				context.getString(R.string.pref_suggestCategory_always).equals(suggestPrefVal);
		boolean suggestUnmatchedOnly =
				context.getString(R.string.pref_suggestCategory_unmatched).equals(suggestPrefVal);
		List<CategorySuggestion<Long>> suggestions = null;
		if (suggestAlways || suggestUnmatchedOnly || suggestForced) {
			if (!suggestForced && Category.SKIP_SUGGEST.contains(currentTypeName)) {
				suggestions = Collections.emptyList();
			} else {
				suggestions = doSuggest(userInput);
			}
			if (!suggestions.isEmpty()) {
				if (suggestUnmatchedOnly && !suggestForced) {
					if (isSuggested(suggestions, currentTypeName)) {
						suggestions = null;
					}
				}
			} else {
				if (suggestForced) {
					suggestions = Collections.emptyList();
				} else if (!Category.MUST_SUGGEST.contains(currentTypeName)) {
					suggestions = null;
				}
			}
		}
		adapter.setSuggestions(suggestions);
		if (suggestions != null) {
			adapter.setCurrentTypeName(currentTypeName);
		}
		return suggestions != null;
	}

	private boolean isSuggested(List<CategorySuggestion<Long>> suggestions, String currentTypeName) {
		CategoryCache cache = CategoryDTO.getCache(context);
		for (CategorySuggestion<Long> category : suggestions) {
			if (cache.getCategoryKey(category.getId()).equals(currentTypeName)) {
				return true;
			}
		}
		return false;
	}

	private List<CategorySuggestion<Long>> doSuggest(CharSequence userInput) {
		List<CategorySuggestion<Long>> suggestions =
				new ArrayList<>(CategoryDTO.getCache(context).suggest(userInput));
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
				if (diff == 0) {
					diff = o1.getId().compareTo(o2.getId());
				}
				return diff;
			}
		});
		return suggestions;
	}
	public RecyclerView.Adapter<?> getAdapter() {
		return adapter;
	}

	private static class HintBuilder extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final long MORE_ID = Category.ID_ADD;
		private final CategoryCache cache;
		private final CategorySelectedEvent clickHandler;
		private List<CategorySuggestion<Long>> suggestions;
		private int upUntil;
		private int maxDistance;
		private String currentTypeName;
		private boolean showEditDistances;

		public HintBuilder(Context context, List<CategorySuggestion<Long>> suggestions,
				CategorySelectedEvent clickHandler) {
			this.cache = CategoryDTO.getCache(context);
			setHasStableIds(true);
			this.clickHandler = clickHandler;
			setSuggestions(suggestions);
		}

		public void setCurrentTypeName(String currentTypeName) {
			this.currentTypeName = currentTypeName;
			notifyItemRangeChanged(0, getItemCount());
		}
		public void setShowEditDistances(boolean showEditDistances) {
			this.showEditDistances = showEditDistances;
			notifyItemRangeChanged(0, getItemCount());
		}
		public void setThreshold(int distance) {
			this.maxDistance = distance;
			int startingFrom = upUntil;
			for (CategorySuggestion<Long> suggestion : suggestions.subList(startingFrom, suggestions.size())) {
				if (suggestion.getMinDistance() <= distance) {
					upUntil++;
				} else {
					break;
				}
			}
			notifyItemRangeChanged(0, startingFrom + 1); // maxDistance conditions may render differently, more moved
			notifyItemRangeInserted(startingFrom + 1, upUntil - startingFrom); // upUntil expanded the list size
			if (upUntil == 0) {
				// if the above didn't find any matches, but there's more to load,
				// show them, even if they're not as good matches
				loadMore();
			}
		}
		public void setSuggestions(List<CategorySuggestion<Long>> suggestions) {
			if (suggestions == null) {
				suggestions = Collections.emptyList();
			}
			this.suggestions = suggestions;
			this.upUntil = 0;
			notifyDataSetChanged();
			setThreshold(0);
		}

		@Override public int getItemViewType(int position) {
			return position != upUntil? HintHolder.TYPE : MoreHolder.TYPE;
		}
		@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			switch (viewType) {
				case HintHolder.TYPE: {
					View view = inflater.inflate(R.layout.item_suggestion, parent, false);
					return new HintHolder(view);
				}
				case MoreHolder.TYPE: {
					View view = inflater.inflate(R.layout.item_suggestion_more, parent, false);
					return new MoreHolder(view);
				}
				default:
					throw new IllegalArgumentException("Unsupported view type: " + viewType);
			}
		}
		@Override public long getItemId(int position) {
			return position != upUntil? suggestions.get(position).getId() : MORE_ID;
		}
		@Override public int getItemCount() {
			return upUntil + (hasMore() || suggestions.isEmpty()? 1 : 0);
		}
		private boolean hasMore() {
			return upUntil != suggestions.size();
		}
		private void loadMore() {
			if (hasMore()) {
				// upUntil is the item that is not shown, the more item is visible at that position
				// it's a valid index in the list, otherwise the more item is not shown
				setThreshold(suggestions.get(upUntil).getMinDistance());
			}
		}
		@Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			if (holder.getItemViewType() == HintHolder.TYPE) {
				((HintHolder)holder).bind(suggestions.get(position));
			} else if (holder.getItemViewType() == MoreHolder.TYPE) {
				((MoreHolder)holder).bind();
			}
		}

		private class MoreHolder extends RecyclerView.ViewHolder {
			private static final int TYPE = 1;
			private final TextView details;
			public MoreHolder(View itemView) {
				super(itemView);
				details = (TextView)itemView.findViewById(R.id.details);
				itemView.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						loadMore();
					}
				});
			}
			public void bind() {
				if (suggestions.isEmpty()) {
					details.setText(R.string.category_suggest_empty);
				} else {
					details.setText(R.string.category_suggest_show_more);
				}
			}
		}

		private class HintHolder extends RecyclerView.ViewHolder {
			private static final int TYPE = 0;
			private final ImageView image;
			private final TextView check;
			private final TextView title;
			private final TextView details;
			private CategorySuggestion<Long> bound;

			public HintHolder(View itemView) {
				super(itemView);
				image = (ImageView)itemView.findViewById(R.id.image);
				check = (TextView)itemView.findViewById(R.id.type);
				title = (TextView)itemView.findViewById(R.id.title);
				details = (TextView)itemView.findViewById(R.id.details);
				itemView.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						clickHandler.categorySelected(bound.getId());
					}
				});
				itemView.setOnLongClickListener(new OnLongClickListener() {
					@Override public boolean onLongClick(View v) {
						clickHandler.categoryQueried(bound.getId());
						return true;
					}
				});
			}

			public void bind(CategorySuggestion<Long> categorySuggestion) {
				bound = categorySuggestion;

				String categoryIcon = cache.getIcon(categorySuggestion.getId());
				int typeId = AndroidTools.getRawResourceID(image.getContext(), categoryIcon);
				Pic.svg().load(typeId).into(image);

				boolean isCurrent = cache.getCategoryKey(categorySuggestion.getId()).equals(currentTypeName);
				AndroidTools.displayedIf(check, isCurrent);

//				title.setText(cache.getCategoryKey(categorySuggestion.getId()).replace("category_", ""));
				title.setText(cache.getCategoryPath(categorySuggestion.getId()));
				details.setText(buildHint(categorySuggestion));
			}

			private @NonNull SpannableStringBuilder buildHint(CategorySuggestion<Long> categorySuggestion) {
				SpannableStringBuilder builder = new SpannableStringBuilder();
				boolean first = true;
				for (KeywordSuggestion<Long> keywordSuggestion : categorySuggestion) {
					if (!keywordAllowed(keywordSuggestion)) {
						continue;
					}
					if (!first) {
						builder.append(", ");
					}
					appendKeyword(builder, keywordSuggestion);
					first = false;
				}
				return builder;
			}

			private void appendKeyword(SpannableStringBuilder builder, KeywordSuggestion<Long> keywordSuggestion) {
				int keywordStart = builder.length();
				builder.append(keywordSuggestion.getKeyword());
				for (WordSuggestion<Long> wordSuggestion : keywordSuggestion) {
					int start = keywordStart + wordSuggestion.getKeywordMatchStart();
					int end = keywordStart + wordSuggestion.getKeywordMatchEnd();
					builder.setSpan(new StyleSpan(Typeface.BOLD),
							start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				if (showEditDistances) {
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
					boolean first = true;
					for (WordSuggestion<Long> wordSuggestion : all) {
						if (maxDistance < wordSuggestion.getDistance()) {
							continue;
						}
						String dist = String.valueOf(wordSuggestion.getDistance());
						int start = keywordStart + wordSuggestion.getKeywordMatchEnd();
						int end = start + dist.length();
						if (!first) {
							builder.insert(start, ",");
							builder.setSpan(new SuperscriptSpan(), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
							builder.setSpan(new RelativeSizeSpan(0.6f), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						builder.insert(start, dist);
						first = false;
						builder.setSpan(new SuperscriptSpan(), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(new RelativeSizeSpan(0.6f), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
						int color = PictureHelper.getColor(wordSuggestion.getInputWord().toString());
						builder.setSpan(new ForegroundColorSpan(color), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}

			private boolean keywordAllowed(KeywordSuggestion<Long> keywordSuggestion) {
				boolean hasWordUnderThreshold = false;
				for (WordSuggestion<Long> wordSuggestion : keywordSuggestion) {
					if (wordSuggestion.getDistance() <= maxDistance) {
						hasWordUnderThreshold = true;
						break;
					}
				}
				return hasWordUnderThreshold;
			}
		}
	}

	public interface CategorySelectedEvent {
		void categorySelected(long categoryID);
		void categoryQueried(long categoryID);
	}
}
