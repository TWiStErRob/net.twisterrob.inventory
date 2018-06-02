package net.twisterrob.java.collections;

import java.util.*;

/** @see <a href="http://stackoverflow.com/a/5475086/253468">How to implement FilteringIterator?</a> */
public class FilteredIterator<T> implements Iterator<T> {
	public interface Filter<T> {
		/**
		 * Determines whether elements should be filtered or not.
		 *
		 * @param element the element to be matched against the filter
		 * @return {@code true} if the element matches the filter, otherwise {@code false}
		 */
		boolean matches(T element);
	}

	private final Iterator<? extends T> iterator;
	private final Filter<? super T> filter;
	private boolean hasNext;
	private T nextElement;

	/**
	 * Creates a new FilteredIterator using wrapping the iterator and returning only elements matching the filter.
	 *
	 * @param iterator
	 *            the iterator to wrap
	 * @param filter
	 *            elements must match this filter to be returned
	 */
	public FilteredIterator(Iterator<? extends T> iterator, Filter<? super T> filter) {
		this.iterator = iterator;
		this.filter = filter;
	}

	@Override public boolean hasNext() {
		return hasNext || tryFindNext();
	}

	@Override public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		hasNext = false;
		return nextElement;
	}

	private boolean tryFindNext() {
		while (iterator.hasNext()) {
			T nextCandidate = iterator.next();
			if (filter.matches(nextCandidate)) {
				nextElement = nextCandidate;
				return hasNext = true;
			}
		}
		return false;
	}

	@Override public void remove() {
		iterator.remove();
	}
}
