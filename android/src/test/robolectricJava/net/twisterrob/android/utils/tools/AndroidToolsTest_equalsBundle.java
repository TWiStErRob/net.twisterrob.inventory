package net.twisterrob.android.utils.tools;

import org.hamcrest.*;
import org.junit.*;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import android.os.Bundle;

import net.twisterrob.android.utils.tostring.stringers.AndroidStringerRepo;
import net.twisterrob.inventory.android.TestIgnoreApp;
import net.twisterrob.java.utils.tostring.StringerRepo;
import net.twisterrob.test.frameworks.RobolectricTestBase;

import static net.twisterrob.test.android.BundleFactory.*;

@Config(application = TestIgnoreApp.class)
public class AndroidToolsTest_equalsBundle extends RobolectricTestBase {
	@Before public void initializeStringer() {
		StringerRepo.INSTANCE.initDefaults();
		AndroidStringerRepo.init(StringerRepo.INSTANCE, RuntimeEnvironment.application);
	}

	@After public void cleanupStringer() {
		StringerRepo.INSTANCE.clear();
	}

	@Test public void testNulls() {
		assertEquals(null, null);
		assertNotEquals(null, new Bundle());
		assertNotEquals(new Bundle(), null);
	}

	@Test public void testEmpty() {
		Bundle same = new Bundle();
		assertEquals("empty equal to self", same, same);
		assertEquals("different fresh objects", new Bundle(), new Bundle());
		assertEquals("capacity shouldn't matter", new Bundle(1), new Bundle(2));
	}

	@Test public void testSameKeysSameValues() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();

		b1.putInt("key1", 1);
		b2.putInt("key1", 1);
		assertEquals(b1, b2);

		b1.putInt("key2", 2);
		b2.putInt("key2", 2);
		assertEquals(b1, b2);
	}

	@Test public void testSameKeysSameAndDifferentValues() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();

		b1.putInt("key1", 1);
		b2.putInt("key1", 1);
		assertEquals(b1, b2);

		b1.putInt("key2", 3);
		b2.putInt("key2", 4);
		assertNotEquals(b1, b2);
	}

	@Test public void testSameKeysDifferentValues() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();

		b1.putInt("key1", 1);
		b2.putInt("key1", 2);
		assertNotEquals(b1, b2);

		b1.putInt("key2", 3);
		b2.putInt("key2", 4);
		assertNotEquals(b1, b2);
	}

	@Test public void testDifferentKeys() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();

		b1.putInt("key1", 1);
		b2.putInt("key2", 2);
		assertNotEquals(b1, b2);

		b1.putInt("key3", 3);
		b2.putInt("key4", 4);
		assertNotEquals(b1, b2);
	}

	@Test public void testAsymmetric() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putInt("key1", 1);

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testAsymmetricKey() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putString("key1", null);
		b2.putString("key2", null);

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testAsymmetricValue() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putString("key", "value");
		b2.putString("key", null);

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testAsymmetricType() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putInt("key", 1);
		b2.putString("key", "value");

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testAsymmetricArrayType() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putStringArray("key", new String[] {"value"});
		b2.putString("key", "value");

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testAsymmetricArrayValue() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putStringArray("key", new String[] {"value1"});
		b2.putStringArray("key", new String[] {"value2"});

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	// CONSIDER parametrize?
	@Test public void testDifferentTypes() {
		assertEquals(primitives(), primitives());
		assertEquals(strings(), strings());
		assertEquals(primitiveArrays(), primitiveArrays());
		assertEquals(objects(), objects());
		assertEquals(allTypes(), allTypes());
	}

	@Test public void testNesting() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putBundle("nested", allTypes());
		b2.putBundle("nested", allTypes());

		assertEquals(b1, b2);
	}

	@Test public void testNestingMixed() {
		Bundle b1 = allTypes();
		Bundle b2 = allTypes();
		b1.putBundle("nested", allTypes());
		b2.putBundle("nested", allTypes());

		assertEquals(b1, b2);
	}

	@Test public void testNestingDifferentBundles() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		Bundle nested = new Bundle();
		nested.putString("key", "asymmteric");
		b1.putBundle("nested", nested);
		b2.putBundle("nested", new Bundle());

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	@Test public void testNestingDifferentTypes() {
		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.putBundle("nested", new Bundle());
		b2.putString("nested", "other bundle is not a bundle");

		assertNotEquals(b1, b2);
		assertNotEquals(b2, b1);
	}

	private static void assertEquals(final Bundle b1, final Bundle b2) {
		assertThat(b1, new CallTargetTestingMethodInAMatcher(b2));
	}
	private static void assertEquals(String message, final Bundle b1, final Bundle b2) {
		assertThat(message, b1, new CallTargetTestingMethodInAMatcher(b2));
	}
	private static void assertNotEquals(final Bundle b1, final Bundle b2) {
		assertThat(b1, not(new CallTargetTestingMethodInAMatcher(b2)));
	}

	private static class CallTargetTestingMethodInAMatcher extends DiagnosingMatcher<Bundle> {
		private final Bundle b2;
		public CallTargetTestingMethodInAMatcher(Bundle b2) {
			this.b2 = b2;
		}
		@Override protected boolean matches(Object item, Description mismatchDescription) {
			Bundle b1 = (Bundle)item;
			if (!AndroidTools.equals(b1, b2)) {
				mismatchDescription.appendText("was ");
				mismatchDescription.appendText(StringerTools.toString(b1));
				return false;
			}
			return true;
		}
		@Override public void describeTo(Description description) {
			description.appendText(StringerTools.toString(b2));
		}
	}
}
