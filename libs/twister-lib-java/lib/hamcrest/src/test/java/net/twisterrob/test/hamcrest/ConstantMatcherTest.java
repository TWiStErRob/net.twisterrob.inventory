package net.twisterrob.test.hamcrest;

import org.junit.*;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class ConstantMatcherTest {

	private static final String FIELD_VALUE = "field value";

	@Test public void testFieldDoesNotExist() {
		final String fieldName = "non-existent field";
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateField.class, new ConstantMatcher(fieldName, anything()));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString(fieldName)));
		assertThat(expectedFailure, hasMessage(containsString(NoSuchFieldException.class.getSimpleName())));
	}

	@Test public void testInstanceFieldIsNotStatic() {
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateField.class, new ConstantMatcher("privateField", MatcherTestHelpers.TRUE_MATCHER));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString("was non-static field")));
		assertThat(expectedFailure, hasMessage(containsString("privateField")));
		assertThat(expectedFailure, hasMessage(containsString(
				"static final class member named \"privateField\" with value "
						+ MatcherTestHelpers.TRUE_MATCHER_DESCRIPTION)));
	}

	@Test public void testFinalInstanceFieldIsNotStatic() {
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateFinalField.class,
						new ConstantMatcher("privateFinalField", MatcherTestHelpers.TRUE_MATCHER));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString("was non-static field")));
		assertThat(expectedFailure, hasMessage(containsString("privateFinalField")));
		assertThat(expectedFailure, hasMessage(containsString(
				"static final class member named \"privateFinalField\" with value "
						+ MatcherTestHelpers.TRUE_MATCHER_DESCRIPTION)));
	}

	@Test public void testStaticFieldIsNotFinal() {
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateStaticField.class,
						new ConstantMatcher("privateStaticField", MatcherTestHelpers.TRUE_MATCHER));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString("was non-final class field")));
		assertThat(expectedFailure, hasMessage(containsString("privateStaticField")));
		assertThat(expectedFailure, hasMessage(containsString(
				"static final class member named \"privateStaticField\" with value "
						+ MatcherTestHelpers.TRUE_MATCHER_DESCRIPTION)));
	}

	@Test public void testConstantIsMatched() {
		assertThat(PrivateStaticFinalField.class, new ConstantMatcher("privateConstantField", anything()));
	}

	@Test public void testConstantIsMatchedWithMatcher() {
		assertThat(PrivateStaticFinalField.class,
				new ConstantMatcher("privateConstantField", equalTo(FIELD_VALUE)));
	}

	@Test public void testConstantIsNotMatched() {
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateStaticFinalField.class,
						new ConstantMatcher("privateConstantField", MatcherTestHelpers.FALSE_MATCHER));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString(
				"static final class member named \"privateConstantField\" with value "
						+ MatcherTestHelpers.FALSE_MATCHER_DESCRIPTION)));
		assertThat(expectedFailure, hasMessage(containsString("was \"" + FIELD_VALUE + "\"")));
	}

	@Ignore("Couldn't get an IllegalAccessException via security")
	@SuppressWarnings({"JUnit3StyleTestMethodInJUnit4Class", "unused"})
	public void testReflectionNotAllowed() {
		// Tried below two options, but neither worked.
		//Security.setProperty("package.access", ConstantMatcher.class.getPackage().getName());
		//System.setSecurityManager with override checkPackageAccess(String pkg) =
		//	if (pkg.equals(ConstantMatcher.class.getPackage().getName())) {
		//		throw new SecurityException("Reflection is not allowed!");
		//	}
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(PrivateStaticFinalField.class,
						new ConstantMatcher("privateConstantField", MatcherTestHelpers.TRUE_MATCHER));
			}
		});
		assertThat(expectedFailure, hasMessage(containsString(IllegalAccessException.class.getSimpleName())));
	}

	@SuppressWarnings("unused")
	private static class PrivateField {
		private String privateField = FIELD_VALUE;
	}

	@SuppressWarnings("unused")
	private static class PrivateStaticField {
		private static String privateStaticField = FIELD_VALUE;
	}

	@SuppressWarnings("unused")
	private static class PrivateFinalField {
		private final String privateFinalField = FIELD_VALUE;
	}

	@SuppressWarnings("unused")
	private static class PrivateStaticFinalField {
		private static final String privateConstantField = FIELD_VALUE;
	}
}
