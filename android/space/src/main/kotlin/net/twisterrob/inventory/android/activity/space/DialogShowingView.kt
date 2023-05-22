package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.use
import net.twisterrob.android.AndroidConstants
import net.twisterrob.inventory.android.space.R

class DialogShowingView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	@AttrRes defStyleAttr: Int = 0,
	@StyleRes defStyleRes: Int = 0,
) : InvisibleView(context, attrs, defStyleAttr, defStyleRes) {

	override var visibilityState: Int
		get() = super.visibilityState
		set(value) {
			super.visibilityState = value
			if (value == VISIBLE) {
				dialog = builder.show()
			} else {
				dialog?.dismiss()
			}
		}

	private lateinit var builder: AlertDialog.Builder
	private var dialog: AlertDialog? = null

	init {
		context.obtainStyledAttributes(attrs, R.styleable.DialogShowingView, defStyleAttr, 0)
			.use { values ->
				createDialog(
					context = context,
					dialogStyle = values.dialogStyle()
				)
			}
	}

	private fun TypedArray.dialogStyle(): Int {
		@StyleRes
		val dialogStyle = getResourceId(
			R.styleable.DialogShowingView_dialogStyle,
			R.style.Theme_AppTheme_Dialog
		)
		require(dialogStyle != AndroidConstants.INVALID_RESOURCE_ID) { "Dialog style must be defined" }
		return dialogStyle
	}

	private fun createDialog(
		context: Context,
		@StyleRes dialogStyle: Int
	) {
		builder = AlertDialog.Builder(context, dialogStyle).apply {
			setCancelable(false)
			setTitle("Title")
			setMessage("Message")
		}
	}

	fun setPositiveButtonListener(listener: () -> Unit) {
		builder.setPositiveButton("OK") { _, _ -> listener() }
	}

	fun setNegativeButtonListener(listener: () -> Unit) {
		builder.setNegativeButton("Cancel") { _, _ -> listener() }
	}

	fun setCancelListener(listener: () -> Unit) {
		builder.setCancelable(true)
		builder.setOnCancelListener { listener() }
	}

	override fun onDetachedFromWindow() {
		dialog?.dismiss()
		super.onDetachedFromWindow()
	}
}
