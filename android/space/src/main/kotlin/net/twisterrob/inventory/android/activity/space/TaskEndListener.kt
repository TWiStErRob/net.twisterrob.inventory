package net.twisterrob.inventory.android.activity.space

internal interface TaskEndListener {

	fun taskDone()

	/** NULL object for the interface  */
	class NoActionTaskEndListener : TaskEndListener {

		override fun taskDone() {
			// ignore
		}
	}
}
