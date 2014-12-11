package net.twisterrob.java.utils;

public abstract class ConcurrentTools {
	private ConcurrentTools() {
		// static class
	}

	/** @deprecated only for debugging */
	@Deprecated
	public static void ignorantSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			// ignore
		}
	}
}
