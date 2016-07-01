package net.twisterrob.java.text;

import java.util.*;

import javax.annotation.Nonnull;

public class EditAllowingIndexer<T> implements Indexer<T> {
	private final TrieNode<T> root = new TrieNode<>((char)-1);
	private final int maxDistance;

	public EditAllowingIndexer(int maxDistance) {
		if (maxDistance < 0) {
			throw new IllegalArgumentException("Maximum edit distance must be >= 0: " + maxDistance);
		}
		this.maxDistance = maxDistance;
	}

	@Override public @Nonnull Collection<MatchResult<T>> match(@Nonnull CharSequence input) {
		String word = clean(input.toString());
		return root.match(word, maxDistance);
	}

	@Override public void add(CharSequence word, T entry) {
		word = clean(word.toString());
		TrieNode<T> curr = root;
		for (int i = 0; i < word.length(); ++i) {
			curr = curr.getOrCreate(word.charAt(i));
		}
		curr.word = word;
		curr.terminals.add(entry);
	}

	private String clean(String word) {
		word = word.toLowerCase(Locale.getDefault());
		return word;
	}

	public int size() {
		return root.deepSize();
	}

	private static class TrieNode<T> {
		final char curr;
		final Map<Character, TrieNode<T>> children = new HashMap<>();
		final Set<T> terminals = new HashSet<>();
		CharSequence word;
		TrieNode(char curr) {
			this.curr = curr;
		}
		TrieNode<T> getOrCreate(char key) {
			TrieNode<T> node = children.get(key);
			if (node == null) {
				node = new TrieNode<>(key);
				children.put(key, node);
			}
			return node;
		}

		int deepSize() {
			int size = terminals.size();
			for (TrieNode<T> child : children.values()) {
				if (child != null) {
					size += child.deepSize();
				}
			}
			return size;
		}

		Set<MatchResult<T>> match(String input, int maxDistance) {
			Map<MatchResult<T>, MatchResult<T>> result = new HashMap<>();
			//System.out.printf("matching %s against %s\n", input, this);
			match(result, input, 0, maxDistance, maxDistance, "");
			return result.keySet();
		}

		private static final boolean DEBUG_PATH = false;
		void match(Map<MatchResult<T>, MatchResult<T>> results, String input, int index, int edits, int maxEdits,
				String path) {
//			System.out.printf("%s\n\t%s[%d]=%c (edits=%d): %s\n",
//					this, input, index, index < input.length()? input.charAt(index) : 0, edits, result);
			if (edits < 0) {
				return;
			}
			String newPath = path;
			if (index >= input.length()) {
//				System.out.println("Found " + terminals);
				for (T source : terminals) {
					MatchResult<T> newer = new MatchResult<>(input, word, maxEdits - edits, source, path);
					MatchResult<T> older = results.get(newer);
					if (older == null || newer.distance < older.distance) {
						results.remove(older);
						results.put(newer, newer);
					}
				}
				for (TrieNode<T> child : children.values()) {
					// add char at the end
					if (DEBUG_PATH) {
						newPath = path + " +[" + index + "=end] added '" + child.curr + "'";
					}
					child.match(results, input, index, edits - 1, maxEdits, newPath);
				}
			} else if (index < input.length()) {
				char inputChar = input.charAt(index);
				TrieNode<T> node = children.get(inputChar);
				if (node != null) {
					// match, go to next char
					if (DEBUG_PATH) {
						newPath = path + " =[" + index + "]'" + inputChar + "'";
					}
					node.match(results, input, index + 1, edits, maxEdits, newPath);
				}
				// delete char (rematch with next char)
				if (DEBUG_PATH) {
					newPath = path + " -[" + index + "]'" + inputChar + "'";
				}
				this.match(results, input, index + 1, edits - 1, maxEdits, newPath);
				for (TrieNode<T> child : children.values()) {
					// add char (pretend child matched)
					if (DEBUG_PATH) {
						newPath = path + " +[" + index + "]'" + child.curr + "'";
					}
					child.match(results, input, index, edits - 1, maxEdits, newPath);
					// change char (pretend this matched)
					if (DEBUG_PATH) {
						newPath = path + " *[" + index + "]'" + inputChar + "'->'" + child.curr + "'";
					}
					child.match(results, input, index + 1, edits - 1, maxEdits, newPath);
				}
				// match a swap as one edit: xxx12yyy -> xxx21yyy
				if (index + 1 < input.length()) { // next position is valid
					TrieNode<T> next1 = children.get(input.charAt(index + 1)); // match this to next char
					if (next1 != null) {
						TrieNode<T> next2 = null;
						// check if any of the children match current char
						for (TrieNode<T> child : children.values()) {
							next2 = child.children.get(inputChar);
							if (next2 != null) {
								break;
							}
						}
						if (next2 != null) { // next1 == 2 and next2 == 1
							// jump after next2, and count one edit
							if (DEBUG_PATH) {
								newPath = path + " /[" + index + "," + (index + 1) + "]"
										+ "'" + next1.curr + "'<->'" + next2.curr + "'";
							}
							next2.match(results, input, index + 2, edits - 1, maxEdits, newPath);
						}
					}
				}
			}
		}

		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			toNiceString(sb, 0);
			return sb.toString();
		}

		public void toNiceString(StringBuilder sb, int depth) {
			String indent = new String(new char[depth]).replace('\0', '\t');
			sb.append(indent).append("{").append(' ');
			sb.append("\"node\": ").append(curr).append(',').append(' ');
			sb.append("\"terminals\": ").append(terminals).append(',').append(' ');
			if (children.isEmpty()) {
				sb.append("[]");
			} else {
				sb.append('[').append('\n');
				for (TrieNode<T> child : children.values()) {
					child.toNiceString(sb, depth + 1);
					sb.append('\n');
				}
				sb.append(indent).append(']');
			}
			sb.append(' ').append('}');
		}
	}
}
