package net.twisterrob.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.inputmethod.*;

import com.google.android.material.textfield.TextInputLayout;

// TODEL https://issuetracker.google.com/issues/37118887
public class TextInputEditText extends com.google.android.material.textfield.TextInputEditText {
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
			// try to work around https://issuetracker.google.com/issues/37118887
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
