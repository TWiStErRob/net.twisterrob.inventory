package net.twisterrob.android.test.espresso;

import org.junit.Rule;

import android.app.AlertDialog;
import android.support.test.rule.ActivityTestRule;

import net.twisterrob.android.test.junit.TestPackageIntentRule;
import net.twisterrob.inventory.android.test.activity.TestActivity;

public class DialogMatchersTest_CloseDialogAndroid extends DialogMatchersTest_CloseDialog {
	@Rule public final ActivityTestRule<TestActivity> activity =
			new TestPackageIntentRule<>(TestActivity.class);

	public DialogMatchersTest_CloseDialogAndroid(
			boolean positive, boolean negative, boolean neutral, boolean cancellable, boolean expectedClosed) {
		super(positive, negative, neutral, cancellable, expectedClosed);
	}

	@SuppressWarnings("Duplicates")
	@Override protected void showDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity.getActivity());
		if (positive) {
			builder.setPositiveButton("positive", null);
		}
		if (negative) {
			builder.setNegativeButton("negative", null);
		}
		if (neutral) {
			builder.setNeutralButton("neutral", null);
		}
		builder.setCancelable(cancellable);
		builder.show();
	}
}
