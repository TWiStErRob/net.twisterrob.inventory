package net.twisterrob.test.frameworks;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.*;
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
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
public abstract class RobolectricTestBase {
	@Rule public MockitoRule mockito = MockitoJUnit.rule();
	@Rule public ExpectedException thrown = ExpectedException.none();
}
