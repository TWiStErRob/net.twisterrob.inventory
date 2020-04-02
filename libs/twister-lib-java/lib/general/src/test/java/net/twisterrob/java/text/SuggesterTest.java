package net.twisterrob.java.text;

import org.junit.*;
import org.mockito.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

import net.twisterrob.java.text.Suggester.DictionaryWord;

@SuppressWarnings("SpellCheckingInspection")
public class SuggesterTest {
	private static final String IGNORED = null;
	@Mock Indexer<DictionaryWord<String>> indexer;
	private Suggester<String> suggester;

	@Before public void initMocks() {
		MockitoAnnotations.initMocks(this);
		suggester = new Suggester<>(indexer, 1);
	}

	@SuppressWarnings("unchecked")
	@Test public void testUnicode() {
		suggester.addText(IGNORED, "English, العربية, 汉语, 漢語, עִבְרִית, ελληνικά, ភាសាខ្មែរ");

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(indexer, atLeast(0)).add(captor.capture(), any(DictionaryWord.class));

		assertThat(captor.getAllValues(), containsInAnyOrder(
				"English",   // English=English
				"العربية",  // al-ʿarabiyya=Arabic
				"漢語",      // hànyǔ=Chinese (Simplified)
				"汉语",      // hànyǔ=Chinese (Simplified)
				"עִבְרִית",    // ivrít=Hebrew
				"ελληνικά",  // elliniká=Greek
				"ភាសាខ្មែរ"       // pʰiesaa kmae=Khmer/Cambodian
		));
	}

	@Test public void testSingleWord() {
		suggester.addWord(IGNORED, "hello");

		verify(indexer).add("hello", new DictionaryWord<>(IGNORED, "hello", 0, 5));
	}

	@Test public void testSingleWordWithSpaces() {
		suggester.addWord(IGNORED, "hello world");

		verify(indexer).add("hello world", new DictionaryWord<>(IGNORED, "hello world", 0, 11));
	}

	@Test public void testSingleKeyword() {
		suggester.addKeyword(IGNORED, "hello");

		verify(indexer).add("hello", new DictionaryWord<>(IGNORED, "hello", 0, 5));
	}

	@Test public void testSingleKeywordMultipleWords() {
		suggester.addKeyword(IGNORED, "hello world");

		verify(indexer).add("hello", new DictionaryWord<>(IGNORED, "hello world", 0, 5));
		verify(indexer).add("world", new DictionaryWord<>(IGNORED, "hello world", 6, 11));
	}

	@Test public void testKeywordMultipleDistinct() {
		suggester.addKeyword(IGNORED, "abc def ghi");
		suggester.addKeyword(IGNORED, "jkl mno");

		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc def ghi", 0, 3));
		verify(indexer).add("def", new DictionaryWord<>(IGNORED, "abc def ghi", 4, 7));
		verify(indexer).add("ghi", new DictionaryWord<>(IGNORED, "abc def ghi", 8, 11));
		verify(indexer).add("jkl", new DictionaryWord<>(IGNORED, "jkl mno", 0, 3));
		verify(indexer).add("mno", new DictionaryWord<>(IGNORED, "jkl mno", 4, 7));
	}

	@Test public void testKeywordMultipleDuplicate() {
		suggester.addKeyword(IGNORED, "abc def");
		suggester.addKeyword(IGNORED, "abc def");

		verify(indexer, times(2)).add("abc", new DictionaryWord<>(IGNORED, "abc def", 0, 3));
		verify(indexer, times(2)).add("def", new DictionaryWord<>(IGNORED, "abc def", 4, 7));
	}

	@Test public void testKeywordMultipleDuplicateAllSame() {
		suggester.addKeyword(IGNORED, "abc abc");
		suggester.addKeyword(IGNORED, "abc abc abc");

		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc abc", 0, 3));
		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc abc", 4, 7));
		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc abc abc", 0, 3));
		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc abc abc", 4, 7));
		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc abc abc", 8, 11));
	}

	@Test public void testTextOneWord() {
		suggester.addText(IGNORED, "hello");

		verify(indexer).add("hello", new DictionaryWord<>(IGNORED, "hello", 0, 5));
	}

	@Test public void testTextMultiWord() {
		suggester.addText(IGNORED, "abc def ghi");

		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc def ghi", 0, 3));
		verify(indexer).add("def", new DictionaryWord<>(IGNORED, "abc def ghi", 4, 7));
		verify(indexer).add("ghi", new DictionaryWord<>(IGNORED, "abc def ghi", 8, 11));
	}

	@Test public void testTextMultiKeyword() {
		suggester.addText(IGNORED, "abc, def, ghi");

		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc", 0, 3));
		verify(indexer).add("def", new DictionaryWord<>(IGNORED, "def", 0, 3));
		verify(indexer).add("ghi", new DictionaryWord<>(IGNORED, "ghi", 0, 3));
	}

	@Test public void testTextMultiKeywordComplex() {
		suggester.addText(IGNORED, "abcdef ghi, jklmno, pqrs tuvw");

		verify(indexer).add("abcdef", new DictionaryWord<>(IGNORED, "abcdef ghi", 0, 6));
		verify(indexer).add("ghi", new DictionaryWord<>(IGNORED, "abcdef ghi", 7, 10));
		verify(indexer).add("jklmno", new DictionaryWord<>(IGNORED, "jklmno", 0, 6));
		verify(indexer).add("pqrs", new DictionaryWord<>(IGNORED, "pqrs tuvw", 0, 4));
		verify(indexer).add("tuvw", new DictionaryWord<>(IGNORED, "pqrs tuvw", 5, 9));
	}

	@Test public void testTextCombined() {
		suggester.addText(IGNORED, "abcdef ghi");
		suggester.addText(IGNORED, "jklmno, pqrs tuvw");

		verify(indexer).add("abcdef", new DictionaryWord<>(IGNORED, "abcdef ghi", 0, 6));
		verify(indexer).add("ghi", new DictionaryWord<>(IGNORED, "abcdef ghi", 7, 10));
		verify(indexer).add("jklmno", new DictionaryWord<>(IGNORED, "jklmno", 0, 6));
		verify(indexer).add("pqrs", new DictionaryWord<>(IGNORED, "pqrs tuvw", 0, 4));
		verify(indexer).add("tuvw", new DictionaryWord<>(IGNORED, "pqrs tuvw", 5, 9));
	}

	@Test public void testEverything() {
		suggester.addWord(IGNORED, "abc");
		suggester.addKeyword(IGNORED, "abcdef ghi");
		suggester.addText(IGNORED, "jklmno, pqrs tuvw");
		suggester.addWord(IGNORED, "def");
		suggester.addText(IGNORED, "abcdef ghi, abcdef");

		verify(indexer).add("abc", new DictionaryWord<>(IGNORED, "abc", 0, 3));
		verify(indexer).add("def", new DictionaryWord<>(IGNORED, "def", 0, 3));
		verify(indexer, times(2)).add("abcdef", new DictionaryWord<>(IGNORED, "abcdef ghi", 0, 6));
		verify(indexer, times(2)).add("ghi", new DictionaryWord<>(IGNORED, "abcdef ghi", 7, 10));
		verify(indexer).add("abcdef", new DictionaryWord<>(IGNORED, "abcdef", 0, 6));
		verify(indexer).add("jklmno", new DictionaryWord<>(IGNORED, "jklmno", 0, 6));
		verify(indexer).add("pqrs", new DictionaryWord<>(IGNORED, "pqrs tuvw", 0, 4));
		verify(indexer).add("tuvw", new DictionaryWord<>(IGNORED, "pqrs tuvw", 5, 9));
	}
}
