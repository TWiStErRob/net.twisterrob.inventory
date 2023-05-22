package net.twisterrob.inventory.android.activity.space

import android.app.Dialog
import android.os.Bundle
import net.twisterrob.inventory.android.fragment.BaseDialogFragment

internal class BlockingTaskProgressDialog : BaseDialogFragment() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isCancelable = false
	}

	@Suppress("DEPRECATION") // blocking the user's view intentionally
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = android.app.ProgressDialog(requireContext())
		dialog.isIndeterminate = true
		//dialog.setTitle(title);
		dialog.setMessage("Please wait...")
		return dialog
	}

	fun taskDone() {
		dismissAllowingStateLoss()
	}
}
