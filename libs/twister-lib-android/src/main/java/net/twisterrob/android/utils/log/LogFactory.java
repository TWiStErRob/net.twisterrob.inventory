package net.twisterrob.android.utils.log;

import java.util.concurrent.atomic.AtomicReferenceArray;

public final class LogFactory {
	private static final AtomicReferenceArray<Log> LOGS = new AtomicReferenceArray<Log>(Tag.values().length);

	private LogFactory() {
		// prevent instantiation
	}

	public static Log getLog(final Tag tag) {
		Log ret = LOGS.get(tag.ordinal());
		if (ret == null) {
			synchronized (LOGS) {
				ret = LOGS.get(tag.ordinal());
				if (ret == null) {
					ret = new Log(tag);
					LOGS.set(tag.ordinal(), ret);
				}
			}
		}
		return ret;
	}
}
