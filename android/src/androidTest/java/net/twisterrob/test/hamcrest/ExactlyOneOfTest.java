package net.twisterrob.test.hamcrest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.hamcrest.*;
import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

/**
 * Tests based on {@link org.hamcrest.core.AnyOfTest}.
 */
public class ExactlyOneOfTest {
	@Test public void copesWithNulls() {
		Matcher<String> matcher = exactlyOneOf(equalTo("irrelevant"), startsWith("irr"));

		assertFalse(matcher.matches(null));
	}
	@Test public void copesWithUnknownTypes() {
		Matcher<String> matcher = exactlyOneOf(equalTo("irrelevant"), startsWith("irr"));

		assertFalse(matcher.matches(0));
	}

	@Test public void evaluatesToTheTheLogicalExclusiveDisjunctionOfTwoOtherMatchers() {
		Matcher<String> matcher = exactlyOneOf(startsWith("goo"), endsWith("ood"));

		assertDoesNotMatch("didn't pass exclusivity of sub-matchers", matcher, "good");
		assertMatches("didn't pass second sub-matcher", matcher, "mood");
		assertMatches("didn't pass first sub-matcher", matcher, "goon");
		assertDoesNotMatch("didn't fail both sub-matchers", matcher, "flan");
	}

	@Test public void evaluatesToTheTheLogicalExclusiveDisjunctionOfManyOtherMatchers() {
		Matcher<String> matcher =
				exactlyOneOf(startsWith("g"), startsWith("go"), endsWith("d"), startsWith("go"), startsWith("goo"));

		assertMatches("didn't pass middle sub-matcher", matcher, "vlad");
		assertDoesNotMatch("didn't fail all sub-matchers", matcher, "flan");
		assertDoesNotMatch("didn't pass exclusivity of sub-matchers", matcher, "glad");
	}

	@SuppressWarnings("unchecked")
	@Test public void supportsMixedTypes() {
		final Matcher<List<String>> matcher = exactlyOneOf(
				instanceOf(ArrayList.class),
				hasItems("hello"),
				hasItems(greaterThan("world"))
		);

		assertMatches("didn't pass hello sub-matcher", matcher, Arrays.asList("hello", "world"));
		assertMatches("didn't fail second and third sub-matchers", matcher, new ArrayList<String>());
		assertDoesNotMatch("didn't fail hasItems sub-matchers", matcher, Arrays.asList("hello", "zzz"));
		assertMatches("world sub-matcher evaluated twice", matcher, Arrays.asList("xxx", "zzz"));
	}

	@Test public void supportsCollections() {

	}

	@SuppressWarnings("serial") // won't be serialized
	@Test public void supportsMaps() {
		Matcher<Map<String, Integer>> matcher =
				exactlyOneOf(hasKey("good"), hasValue(greaterThan(0)), hasEntry("yay", 0));

		assertMatches("didn't fail > 0 and yay sub-matchers", matcher, new HashMap<String, Integer>() {{
			put("good", 0);
		}});
		assertMatches("didn't fail good and yay sub-matchers", matcher, new HashMap<String, Integer>() {{
			put("yay", 1);
		}});
		assertMatches("didn't fail good and > 0 sub-matchers", matcher, new ConcurrentHashMap<String, Integer>() {{
			put("yay", -1);
			put("yay", 0);
		}});
		assertDoesNotMatch("didn't fail when good and > 0 matched", matcher, new TreeMap<String, Integer>() {{
			put("good", 0);
			put("good", 1);
		}});
	}

	@Test public void hasAReadableDescription() {
		Description description = new StringDescription();

		description.appendDescriptionOf(exactlyOneOf(equalTo("good"), equalTo("bad"), equalTo("ugly")));

		Assert.assertEquals("Expected description",
				"exactly one of (\"good\" or \"bad\" or \"ugly\")", description.toString().trim());
	}

	private static <T> void assertMatches(String message, Matcher<? super T> matcher, T arg) {
		if (!matcher.matches(arg)) {
			Description description = new StringDescription();
			matcher.describeMismatch(arg, description);
			Assert.fail(message + " because: '" + description.toString().trim() + "'");
		}
	}

	private static <T> void assertDoesNotMatch(String message, Matcher<? super T> matcher, T arg) {
		assertFalse(message, matcher.matches(arg));
	}
}
