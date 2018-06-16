package net.twisterrob.java.text;

import java.util.*;
import java.util.regex.*;

import javax.annotation.Nonnull;

import net.twisterrob.java.collections.WeakValueHashMap;

@SuppressWarnings({"unused", "WeakerAccess", "SimplifiableIfStatement"})
public class Suggester<T> {
	/** Non-static to be able to use it from multiple threads. */
	private final Pattern keywordSplitter = Pattern.compile("[,;/()]+");
	/** Non-static to be able to use it from multiple threads. */
	private final Pattern wordPattern;
	private final Indexer<DictionaryWord<T>> index;
	private final WeakValueHashMap<WordMatch, Collection<Indexer.MatchResult<DictionaryWord<T>>>> cache =
			new WeakValueHashMap<>();

	public Suggester(Indexer<DictionaryWord<T>> index, int minLength) {
		if (minLength < 0) {
			throw new IllegalArgumentException("Minimum length must be >= 0: " + minLength);
		}
		this.index = index;
		this.wordPattern = Pattern.compile("(\\p{L}\\p{M}*+|[']){" + minLength + ",}");
	}

	public Iterable<WordMatch> split(final CharSequence input) {
		return new Iterable<WordMatch>() {
			@Override public Iterator<WordMatch> iterator() {
				return new Iterator<WordMatch>() {
					private final Matcher m = wordPattern.matcher(input);
					@Override public boolean hasNext() {
						return m.find();
					}
					@Override public WordMatch next() {
						return new WordMatch(input, m.start(), m.end());
					}
					@Override public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public @Nonnull Collection<CategorySuggestion<T>> suggest(@Nonnull CharSequence input) {
		Map<T, CategorySuggestion<T>> suggestions = new HashMap<>();
		for (WordMatch wordMatch : split(input)) {
			Collection<Indexer.MatchResult<DictionaryWord<T>>> matches = cache.getValue(wordMatch);
			if (matches == null) {
				matches = index.match(wordMatch.word());
				cache.putValue(wordMatch, matches);
			}
			for (Indexer.MatchResult<DictionaryWord<T>> match : matches) {
				T id = match.source.id;
				CategorySuggestion<T> suggestion = suggestions.get(id);
				if (suggestion == null) {
					suggestion = new CategorySuggestion<>(id);
					suggestions.put(id, suggestion);
				}
				suggestion.add(wordMatch, match);
			}
		}

		return suggestions.values();
	}

	public static class CategorySuggestion<T> implements Iterable<KeywordSuggestion<T>> {
		private final T id;
		private final Map<CharSequence, KeywordSuggestion<T>> data = new HashMap<>();
		private int minDistance = Integer.MAX_VALUE;
		private int maxDistance = Integer.MIN_VALUE;
		private final Map<Integer, Integer> distanceCounts = new HashMap<>();

		CategorySuggestion(T id) {
			this.id = id;
		}

		public T getId() {
			return id;
		}

		public int getMinDistance() {
			return minDistance;
		}

		public int getMaxDistance() {
			return maxDistance;
		}

		public int getDistanceCount(int distance) {
			Integer count = distanceCounts.get(distance);
			return count != null? count : 0;
		}

		void add(WordMatch match, Indexer.MatchResult<DictionaryWord<T>> result) {
			CharSequence key = result.source.keyword;
			KeywordSuggestion<T> keyword = data.get(key);
			if (keyword == null) {
				keyword = new KeywordSuggestion<>(result.source);
				data.put(key, keyword);
			}
			WordSuggestion<T> word = new WordSuggestion<>(match, result);
			keyword.add(word);
			updateStats(word);
		}

		private void updateStats(WordSuggestion<T> word) {
			int dist = word.getDistance();
			minDistance = Math.min(minDistance, dist);
			maxDistance = Math.max(maxDistance, dist);

			Integer count = distanceCounts.get(dist);
			count = count != null? count + 1 : 0;
			distanceCounts.put(dist, count);
		}

		@Override public Iterator<KeywordSuggestion<T>> iterator() {
			return data.values().iterator();
		}
	}

	public static class KeywordSuggestion<T> implements Iterable<WordSuggestion<T>> {
		private final DictionaryWord<T> keyword;
		private final List<WordSuggestion<T>> data = new ArrayList<>();

		KeywordSuggestion(DictionaryWord<T> keyword) {
			this.keyword = keyword;
		}

		public CharSequence getKeyword() {
			return keyword.keyword;
		}

		@Override public Iterator<WordSuggestion<T>> iterator() {
			return data.iterator();
		}

		void add(WordSuggestion<T> suggestion) {
			data.add(suggestion);
		}
	}

	public static class WordSuggestion<T> {
		private final WordMatch word;
		private final Indexer.MatchResult<DictionaryWord<T>> result;

		WordSuggestion(WordMatch word, Indexer.MatchResult<DictionaryWord<T>> result) {
			this.word = word;
			this.result = result;
		}
		public T getSource() {
			return result.source.id;
		}
		public CharSequence getInput() {
			return word.input;
		}
		public CharSequence getInputWord() {
			return word.word();
		}
		public CharSequence getInputMatch() {
			return result.search;
		}
		public CharSequence getKeyword() {
			return result.source.keyword;
		}
		public CharSequence getKeywordMatch() {
			return result.source.word();
		}
		public int getKeywordMatchStart() {
			return result.source.wordStart;
		}
		public int getKeywordMatchEnd() {
			return result.source.wordEnd;
		}
		public int getDistance() {
			return result.distance;
		}
	}

	/** Split text along list separators to extract keywords. */
	public void addText(T id, @Nonnull CharSequence text) {
		String[] keywords = keywordSplitter.split(text);
		for (String keyword : keywords) {
			keyword = keyword.trim();
			if (keyword.length() != 0) {
				addKeyword(id, keyword);
			}
		}
	}

	/** Split keywords along word separators to extract words. */
	public void addKeyword(T id, @Nonnull String keyword) {
		Matcher m = wordPattern.matcher(keyword);
		while (m.find()) {
			addIndex(new DictionaryWord<>(id, keyword, m.start(), m.end()));
		}
	}

	/** Add a raw word into the index. */
	public void addWord(T id, @Nonnull String word) {
		addIndex(new DictionaryWord<>(id, word));
	}

	private void addIndex(DictionaryWord<T> entry) {
		index.add(entry.word(), entry);
	}

	public static class WordMatch {
		public final @Nonnull CharSequence input;
		public final int wordStart;
		public final int wordEnd;
		private final CharSequence word;

		public WordMatch(@Nonnull CharSequence input, int start, int end) {
			this.input = input;
			this.wordStart = start;
			this.wordEnd = end;
			this.word = input.subSequence(wordStart, wordEnd);
		}

		public CharSequence word() {
			return word;
		}

		@Override public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof WordMatch)) {
				return false;
			}

			WordMatch match = (WordMatch)o;

			return word.equals(match.word);
		}

		@Override public int hashCode() {
			return word.hashCode();
		}

		@Override public String toString() {
			return String.format(Locale.ROOT, "%s[%d,%d)=%s", input, wordStart, wordEnd, word);
		}
	}

	public static class DictionaryWord<T> {
		final T id;
		final String keyword;
		final int wordStart;
		final int wordEnd;

		DictionaryWord(T id, String word) {
			this(id, word, 0, word.length());
		}

		DictionaryWord(T id, String keyword, int wordStart, int wordEnd) {
			this.id = id;
			this.keyword = keyword;
			this.wordStart = wordStart;
			this.wordEnd = wordEnd;
		}

		public String word() {
			return keyword.substring(wordStart, wordEnd);
		}

		@Override public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof DictionaryWord)) {
				return false;
			}

			DictionaryWord<?> that = (DictionaryWord<?>)o;

			if (wordStart != that.wordStart) {
				return false;
			}
			if (wordEnd != that.wordEnd) {
				return false;
			}
			if (id != null? !id.equals(that.id) : that.id != null) {
				return false;
			}
			return keyword.equals(that.keyword);
		}

		@Override public int hashCode() {
			int result = id != null? id.hashCode() : 0;
			result = 31 * result + keyword.hashCode();
			result = 31 * result + wordStart;
			result = 31 * result + wordEnd;
			return result;
		}

		@Override public String toString() {
			return String.format(Locale.ROOT, "%s|%s[%d,%d]=%s", id, keyword, wordStart, wordEnd, word());
		}
	}
}
