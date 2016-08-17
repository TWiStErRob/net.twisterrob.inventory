package net.twisterrob.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Base class extended by every Robolectric test in this project.
 * <p/>
 * You can use Powermock together with Robolectric.
 *
 * @see <a href="https://github.com/robolectric/robolectric/wiki/Using-PowerMock">Using PowerMock</a>
 */
@RunWith(RobolectricTestRunner.class)
// TOFIX PowerMock doesn't work with Robolectric 3.1-3.1.2: https://github.com/robolectric/robolectric/issues/2429
//@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
public abstract class DefaultRobolectricTest {
	// Even with 3.0 there are issues
	// @Rule public PowerMockRule rule = new PowerMockRule();

	@Before public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
}
