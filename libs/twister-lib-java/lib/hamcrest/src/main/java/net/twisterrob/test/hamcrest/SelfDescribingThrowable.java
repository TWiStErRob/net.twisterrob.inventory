package net.twisterrob.test.hamcrest;

import org.hamcrest.*;

import net.twisterrob.java.utils.ObjectTools;

class SelfDescribingThrowable implements SelfDescribing {

	private final Throwable item;

	public SelfDescribingThrowable(Throwable item) {
		this.item = item;
	}

	@Override public void describeTo(Description description) {
		description.appendText(ObjectTools.getFullStackTrace(item));
	}
}
