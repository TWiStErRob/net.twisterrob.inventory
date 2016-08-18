package net.twisterrob.test.frameworks.classes;

import android.view.View.OnClickListener;

public class AndroidRecipient {
	private final OnClickListener m;
	public AndroidRecipient(OnClickListener m) {
		this.m = m;
	}
	public OnClickListener getMockable() {
		return m;
	}
}
