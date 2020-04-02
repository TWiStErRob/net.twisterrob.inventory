package net.twisterrob.android.activity;

public interface BackPressAware {
	/**
	 * @return {@code true} if the back press was handled
	 */
	boolean onBackPressed();
}
