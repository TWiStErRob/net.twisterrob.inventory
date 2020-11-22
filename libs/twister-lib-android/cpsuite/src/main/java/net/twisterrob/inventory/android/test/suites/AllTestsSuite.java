package net.twisterrob.inventory.android.test.suites;

import java.io.IOException;
import java.util.*;

import org.junit.extensions.cpsuite.*;
import org.junit.runner.RunWith;
import org.junit.runners.model.*;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.test.internal.runner.*;
import androidx.test.internal.runner.ClassPathScanner.AcceptAllFilter;
import androidx.test.platform.app.InstrumentationRegistry;

/**
 * According to JUnit runner release notes, it doesn't support 9 any more, only 15 and up.
 * @see <a href="https://developer.android.com/training/testing/release-notes#relnotes-20170725-breaking-changes">Runner 1.0.0</a>
 */
@RequiresApi(VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
@RunWith(AllTestsSuite.AndroidClasspathSuite.class)
public class AllTestsSuite {

	private static final String TAG = "AllTestSuite";

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
			String apk = InstrumentationRegistry.getInstrumentation().getContext().getPackageCodePath();
			return new AndroidClasspathClassesFinder(tester, apk);
		}
	}

	static class AndroidClasspathClassesFinder implements ClassesFinder {

		private static final boolean DEBUG_MODE = false;

		private final ClassTester tester;
		private final String apkPath;

		public AndroidClasspathClassesFinder(ClassTester tester, String apkPath) {
			this.tester = tester;
			this.apkPath = apkPath;
		}

		/**
		 * Try to load all classes that are on the classpath to look for tests.
		 * Since we're running in {@link androidx.test.runner.AndroidJUnitRunner}
		 * its superclass {@link androidx.test.runner.MonitoringInstrumentation}
		 * will install {@link androidx.multidex.MultiDex}.
		 */
		@Override public List<Class<?>> find() {
			List<Class<?>> matchedClasses = new ArrayList<>();
			TestLoaderAccess loader = new TestLoaderAccess();
			for (String className : getAllClassNamesOnClassPath()) {
				if (className.startsWith("org.mockito.")
						|| className.startsWith("net.bytebuddy.")
						|| className.startsWith("com.google.common.")
						|| className.startsWith("org.hamcrest.")
						) {
					// Need to filter some packages out because they use classes that are not available.
					// that spams logs with "Rejecting re-init on previously-failed class java.lang.Class<FQCN>"
					continue;
				}
				Class<?> clazz = null;
				try {
					clazz = loader.loadClass(className);
				} catch (NoClassDefFoundError ex) {
					Log.w(TAG, "Cannot load class " + className, ex);
				}
				// clazz may be null when the load fails, just ignore those
				if (clazz != null && tester.acceptClass(clazz)) {
					matchedClasses.add(clazz);
				} else {
					if (DEBUG_MODE) {
						Log.v(TAG, clazz + " was rejected by filters");
					}
				}
			}
			if (DEBUG_MODE && Log.isLoggable(TAG, Log.VERBOSE)) {
				// DEBUG_MODE results in dead code, so UnusedAssignment checks kick in
				@SuppressWarnings("UnusedAssignment")
				String s = matchedClasses.toString();
				@SuppressWarnings("UnusedAssignment")
				String classes = s.substring(1, s.length() - 2).replace(", ", "\n");
				Log.v(TAG, "Matched classes:\n" + classes);
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
