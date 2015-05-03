package net.twisterrob.android.view;

import android.text.*;

public class TextWatcherAdapter implements TextWatcher {
	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// optional override
	}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
		// optional override
	}
	@Override public void afterTextChanged(Editable s) {
		// optional override
	}
}
