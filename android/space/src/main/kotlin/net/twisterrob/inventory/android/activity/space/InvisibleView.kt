package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

open class InvisibleView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	@AttrRes defStyleAttr: Int = 0,
	@StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

	protected open var visibilityState: Int = GONE

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		setMeasuredDimension(0, 0)
	}

	override fun setVisibility(visibility: Int) {
		visibilityState = visibility
	}

	override fun getVisibility(): Int {
		return visibilityState
	}
}
