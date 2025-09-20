package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.junit.*;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import android.os.Build;

import net.twisterrob.test.PackageNameShortener;

/**
 * Base class extended by every Robolectric test in this project.
 * <p/>
 * You can use Powermock together with Robolectric.
 *
 * @see <a href="https://github.com/robolectric/robolectric/wiki/Using-PowerMock">Using PowerMock</a>
 * @see androidx.test.core.app.ApplicationProvider#getApplicationContext
 */
@RunWith(RobolectricTestRunner.class)
//@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(
		// Note: same version is specified robolectric.properties too in case other tests don't use this class.
		// Needs to follow minSdkVersion, otherwise weird Robolectric failures are triggered.
		sdk = Build.VERSION_CODES.M,
		// Enable LogCat in unit tests, see setUp.
		shadows = {ShadowLog.class}
)
//@PowerMockIgnore({"org.mockito.*", "org.powermock.*", "org.robolectric.*", "android.*"})
public abstract class RobolectricTestBase {
	@Rule public final MockitoRule mockito = MockitoJUnit.rule();
	//@Rule public final PowerMockRule power = new PowerMockRule();
	@Rule public final TestRule shortener = new PackageNameShortener();

	@Before
	public void setUp() {
		// MockitoAnnotations.initMocks(this); // done by MockitoRule
		ShadowLog.stream = System.out;
	}
}
