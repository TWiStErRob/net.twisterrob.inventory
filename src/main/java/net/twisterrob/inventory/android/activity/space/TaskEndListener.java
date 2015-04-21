package net.twisterrob.inventory.android.activity.space;

interface TaskEndListener {
	void taskDone();

	/** NULL object for the interface */
	class NoActionTaskEndListener implements TaskEndListener {
		@Override public final void taskDone() {
			// ignore
		}
	}
}
