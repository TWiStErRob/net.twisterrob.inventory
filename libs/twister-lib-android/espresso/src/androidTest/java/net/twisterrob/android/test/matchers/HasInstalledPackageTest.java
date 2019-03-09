package net.twisterrob.android.test.matchers;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static android.support.test.InstrumentationRegistry.*;

public class HasInstalledPackageTest {

	@Test public void missingPackageFails() {
		AssertionError ex = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(getTargetContext(), new HasInstalledPackage("not.existing.package"));
			}
		});
		assertThat(ex.getMessage(), containsString("not.existing.package"));
	}

	@Test public void existingPackagePasses() {
		assertThat(getTargetContext(), new HasInstalledPackage("android"));
		// no exception
	}
}
