package net.twisterrob.test.junit;

import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

import net.twisterrob.java.utils.ConcurrentTools;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class AssertTest {

	@Test public void testExecutionBetweenPasses() {
		Assert.assertTimeout(100, 200, TimeUnit.MILLISECONDS, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override public void run() {
				ConcurrentTools.ignorantSleep(150);
			}
		});
	}
	@Test public void testTimeoutWrongOrder() {
		AssumptionViolatedException expectedFailure =
				assertThrows(AssumptionViolatedException.class, new ThrowingRunnable() {
					@Override public void run() {
						Assert.assertTimeout(200, 100, TimeUnit.MILLISECONDS, new Runnable() {
							@SuppressWarnings("deprecation")
							@Override public void run() {
								ConcurrentTools.ignorantSleep(150);
							}
						});
					}
				});

		assertThat(expectedFailure, hasMessage(containsString("range")));
	}

	@Test public void testTimeoutImmediate() {
		AssumptionViolatedException expectedFailure =
				assertThrows(AssumptionViolatedException.class, new ThrowingRunnable() {
					@Override public void run() {
						Assert.assertTimeout(0, 0, TimeUnit.MILLISECONDS, new Runnable() {
							@SuppressWarnings("deprecation")
							@Override public void run() {
								ConcurrentTools.ignorantSleep(150);
							}
						});
					}
				});

		assertThat(expectedFailure, hasMessage(containsString("range")));
	}

	@Test public void testTimeoutMinimumOutOfRange() {
		AssumptionViolatedException expectedFailure =
				assertThrows(AssumptionViolatedException.class, new ThrowingRunnable() {
					@Override public void run() {
						Assert.assertTimeout(-1, 0, TimeUnit.MILLISECONDS, new Runnable() {
							@SuppressWarnings("deprecation")
							@Override public void run() {
								ConcurrentTools.ignorantSleep(150);
							}
						});
					}
				});

		assertThat(expectedFailure, hasMessage(containsString("minimum")));
	}

	@Test public void testTimeoutMaximumOutOfRange() {
		AssumptionViolatedException expectedFailure =
				assertThrows(AssumptionViolatedException.class, new ThrowingRunnable() {
					@Override public void run() {
						Assert.assertTimeout(0, -1, TimeUnit.MILLISECONDS, new Runnable() {
							@SuppressWarnings("deprecation")
							@Override public void run() {
								ConcurrentTools.ignorantSleep(150);
							}
						});
					}
				});

		assertThat(expectedFailure, hasMessage(containsString("maximum")));
	}

	@Test public void testTooFast() {
		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				Assert.assertTimeout(100, 200, TimeUnit.MILLISECONDS, new Runnable() {
					@SuppressWarnings("deprecation")
					@Override public void run() {
						ConcurrentTools.ignorantSleep(0);
					}
				});
			}
		});
		assertThat(expectedFailure, hasMessage(matchesPattern(
				"Execution finished too fast: \\d+, expected minimum time to execute: 100")));
	}

	@Test public void testTooSlowBetween() {
		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				Assert.assertTimeout(100, 200, TimeUnit.MILLISECONDS, new Runnable() {
					@SuppressWarnings("deprecation")
					@Override public void run() {
						ConcurrentTools.ignorantSleep(300);
					}
				});
			}
		});
		assertThat(expectedFailure, hasMessage(matchesPattern(
				"Execution finished too slow: \\d+, expected maximum time to execute: 200")));
	}

	@Test public void testTooSlow() {
		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				Assert.assertTimeout(100, TimeUnit.MILLISECONDS, new Runnable() {
					@SuppressWarnings("deprecation")
					@Override public void run() {
						ConcurrentTools.ignorantSleep(200);
					}
				});
			}
		});
		assertThat(expectedFailure, hasMessage(matchesPattern(
				"Execution finished too slow: \\d+, expected maximum time to execute: 100")));
	}

	@Test public void testThrow() {
		final NullPointerException fakeException = new NullPointerException("fake failure");
		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				Assert.assertTimeout(100, 200, TimeUnit.MILLISECONDS, new Runnable() {
					@Override public void run() {
						throw fakeException;
					}
				});
			}
		});
		assertThat(expectedFailure, hasMessage(equalTo("Execution failed with exception")));
		assertThat(expectedFailure, hasCause(is(fakeException)));
	}
}
