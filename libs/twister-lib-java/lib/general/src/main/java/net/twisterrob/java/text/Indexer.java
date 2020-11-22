package net.twisterrob.java.text;

import java.util.*;

import javax.annotation.Nonnull;

public interface Indexer<T> {
	@Nonnull Collection<MatchResult<T>> match(@Nonnull CharSequence input);
	void add(CharSequence word, T entry);
	int size();

	class MatchResult<T> {
		/** Suggestion was created for this input. */
		public final @Nonnull CharSequence input;
		public final int distance;
		/** Matched part of index when looking for {@code search}. */
		public final @Nonnull T source;
		private final String path;
		/** Part of input that was looked up in index. */
		public final @Nonnull CharSequence search;

		public MatchResult(@Nonnull CharSequence input, @Nonnull CharSequence search, int distance, @Nonnull T source,
				String path) {
			this.input = input;
			this.search = search;
			this.distance = distance;
			this.source = source;
			this.path = path;
		}

		@Override public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof MatchResult)) {
				return false;
			}

			MatchResult<?> result = (MatchResult<?>)o;
			return input.equals(result.input) && source.equals(result.source) && search.equals(result.search);
		}
		@Override public int hashCode() {
			int result = input.hashCode();
			result = 31 * result + source.hashCode();
			result = 31 * result + search.hashCode();
			return result;
		}
		@Override public @Nonnull String toString() {
			return String.format(Locale.ROOT, "%s/%s(%d) match=%s (%s)", input, search, distance, source, path);
		}
	}
}
