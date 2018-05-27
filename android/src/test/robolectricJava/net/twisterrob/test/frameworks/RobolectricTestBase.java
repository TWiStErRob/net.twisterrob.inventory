package net.twisterrob.test.frameworks;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;
import org.mockito.junit.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import android.os.Build.VERSION_CODES;

import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.test.PackageNameShortener;

/**
 * Base class extended by every Robolectric test in this project.
 * <p/>
 * You can use Powermock together with Robolectric.
 *
 * @see <a href="https://github.com/robolectric/robolectric/wiki/Using-PowerMock">Using PowerMock</a>
 * @see org.robolectric.RuntimeEnvironment#application
 */
@RunWith(RobolectricTestRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
// For this to work make sure the working directory is the app module in the IDE
@Config(constants = BuildConfig.class, sdk = {VERSION_CODES.JELLY_BEAN}, shadows = {ShadowLog.class})
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
public abstract class RobolectricTestBase {
	@Rule public final MockitoRule mockito = MockitoJUnit.rule();
	@Rule public final TestRule shortener = new PackageNameShortener();

	@Before
	public void setUp() {
		// MockitoAnnotations.initMocks(this); // done by MockitoRule
		ShadowLog.stream = System.out;
	}
}
