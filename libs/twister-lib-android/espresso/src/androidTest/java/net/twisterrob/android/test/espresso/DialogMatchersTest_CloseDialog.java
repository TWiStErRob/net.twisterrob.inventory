package net.twisterrob.android.test.espresso;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import android.support.annotation.UiThread;

import net.twisterrob.android.test.junit.InstrumentationExtensions;
import net.twisterrob.test.junit.AndroidJUnit4WithParametersRunnerFactory;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.test.junit.Assert.*;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(AndroidJUnit4WithParametersRunnerFactory.class)
public abstract class DialogMatchersTest_CloseDialog {
	protected final boolean positive;
	protected final boolean negative;
	protected final boolean neutral;
	protected final boolean cancellable;
	private final boolean expectedClosed;

	@Before public void preconditions() {
		assertNoDialogIsDisplayed();
	}

	@Test(timeout = DialogMatchersTest.DIALOG_TIMEOUT) public void test() {
		InstrumentationExtensions.runOnMain(new Runnable() {
			@Override public void run() {
				showDialog();
			}
		});

		attemptCloseDialog_withTimeout();

		if (expectedClosed) {
			assertNoDialogIsDisplayed();
		} else {
			assertDialogIsDisplayed();
		}
	}

	protected static void attemptCloseDialog_withTimeout() {
		assertTimeout(DialogMatchersTest.DECISION_TIMEOUT, TimeUnit.MILLISECONDS, new Runnable() {
			@Override public void run() {
				DialogMatchers.attemptCloseDialog();
			}
		});
	}

	@UiThread
	protected abstract void showDialog();

	public DialogMatchersTest_CloseDialog(
			boolean positive, boolean negative, boolean neutral, boolean cancellable,
			boolean expectedClosed) {
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
		this.cancellable = cancellable;
		this.expectedClosed = expectedClosed;
	}

	@Parameterized.Parameters(name = "{index}: positive={0}, negative={1}, neutral={2}, cancellable={3} -> closed={4}")
	public static Iterable<Object[]> data() {
		List<Object[]> data = new ArrayList<>();
		int booleanArgCount = 4;
		for (int generated = 0; generated < (1 << booleanArgCount); generated++) {
			Object[] args = new Object[booleanArgCount + 1];
			for (int b = 0; b < booleanArgCount; b++) {
				args[b] = (generated & (1 << b)) != 0;
			}
			boolean hasNegative = (Boolean)args[1];
			boolean cancellable = (Boolean)args[3];
			args[booleanArgCount] = cancellable || hasNegative;
			data.add(args);
		}
		return data;
	}
}
