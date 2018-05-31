package net.twisterrob.inventory.android.test.suites;

import java.io.IOException;
import java.util.*;

import org.junit.extensions.cpsuite.*;
import org.junit.runner.RunWith;
import org.junit.runners.model.*;

import android.annotation.SuppressLint;
import android.support.test.InstrumentationRegistry;
import android.support.test.internal.runner.*;
import android.support.test.internal.runner.ClassPathScanner.AcceptAllFilter;

@RunWith(AllTestsSuite.AndroidClasspathSuite.class)
public class AllTestsSuite {
	public static class AndroidClasspathSuite extends ClasspathSuite {
		public AndroidClasspathSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
			super(suiteClass, builder, new AndroidClasspathFinderFactory());
		}
	}

	static class AndroidClasspathFinderFactory implements ClassesFinderFactory {
		@Override public ClassesFinder create(boolean searchInJars, String[] filterPatterns, SuiteType[] suiteTypes,
				Class<?>[] baseTypes, Class<?>[] excludedBaseTypes, String classpathProperty) {
			ClassTester tester = new ClasspathSuiteTester(
					searchInJars, filterPatterns, suiteTypes, baseTypes, excludedBaseTypes);
			String apk = InstrumentationRegistry.getContext().getPackageCodePath();
			return new AndroidClasspathClassesFinder(tester, apk);
		}
	}

	static class AndroidClasspathClassesFinder implements ClassesFinder {
		private final ClassTester tester;
		private String apkPath;

		public AndroidClasspathClassesFinder(ClassTester tester, String apkPath) {
			this.tester = tester;
			this.apkPath = apkPath;
		}

		@Override public List<Class<?>> find() {
			List<Class<?>> matchedClasses = new ArrayList<>();
			// STOPSHIP figure out a way, or is this working already?
			TestLoaderAccess loader = new TestLoaderAccess();
			for (String className : getAllClassNamesOnClassPath()) {
				Class<?> clazz = loader.loadClass(className);
				// clazz may be null when the load fails, just ignore those
				if (clazz != null && tester.acceptClass(clazz)) {
					matchedClasses.add(clazz);
				}
			}
			return matchedClasses;
		}

		/**
		 * Wrapper for using the original {@link ClassPathScanner} inside Espresso.
		 */
		@SuppressLint("VisibleForTests")
		private Iterable<String> getAllClassNamesOnClassPath() {
			try {
				return new ClassPathScanner(apkPath).getClassPathEntries(new AcceptAllFilter());
			} catch (IOException ex) {
				throw new IllegalStateException("Cannot find classes", ex);
			}
		}
	}
}
