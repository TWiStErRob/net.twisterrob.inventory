package net.twisterrob.java.utils;

import net.twisterrob.java.annotations.DebugHelper;

public abstract class ConcurrentTools {
	private ConcurrentTools() {
		// static class
	}

	/** @deprecated only for debugging */
	@Deprecated
	@DebugHelper
	public static void ignorantSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			// ignore
		}
	}
}
