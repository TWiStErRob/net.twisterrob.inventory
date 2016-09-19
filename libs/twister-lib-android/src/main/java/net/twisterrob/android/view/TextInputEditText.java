package net.twisterrob.android.view;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.inputmethod.*;

// TODEL http://b.android.com/221880
public class TextInputEditText extends android.support.design.widget.TextInputEditText {
	public TextInputEditText(Context context) {
		super(context);
	}
	public TextInputEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public TextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		final InputConnection ic = super.onCreateInputConnection(outAttrs);
		if (ic != null && outAttrs.hintText == null) {
			// If we still don't have a hint after the design widget did it's thing,
			// try to work around http://b.android.com/221880
			ViewParent parent = getParent();
			if (parent != null) {
				parent = parent.getParent();
			}
			if (parent instanceof TextInputLayout) {
				outAttrs.hintText = ((TextInputLayout)parent).getHint();
			}
		}
		return ic;
	}
}
