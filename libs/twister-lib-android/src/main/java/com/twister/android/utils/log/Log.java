package com.twister.android.utils.log;

import com.twister.android.utils.tools.StringTools;

/**
 * Class to hide the complexity of Logging in android.
 * 
 * @author Zoltán Kiss
 * @author Róbert Papp
 */
public class Log {
	private final String m_tag;

	/**
	 * Constructs a logger class.
	 * 
	 * @param tag the tag to use in {@link android.util.Log} calls.
	 */
	Log(final Tag tag) {
		m_tag = tag.getTag();
	}

	private static String format(final String messageFormat, final Object... formatArgs) {
		String message = StringTools.format(messageFormat, formatArgs);
		if (message == null || message.trim().length() == 0) {
			throw new IllegalArgumentException("ALERT: Lazy developer! Please describe what you're logging.");
		}
		return message;
	}

	// #region is*Enabled()

	public boolean isVerboseEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.VERBOSE);
	}

	public boolean isDebugEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.DEBUG);
	}

	public boolean isInfoEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.INFO);
	}

	public boolean isWarningEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.WARN);
	}

	public boolean isErrorEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.ERROR);
	}

	public boolean isWTFEnabled() {
		return android.util.Log.isLoggable(m_tag, android.util.Log.ASSERT);
	}

	// #endregion

	// #region Logging methods

	public void verbose(final String messageFormat, final Object... formatArgs) {
		android.util.Log.v(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void verbose(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.v(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	public void debug(final String messageFormat, final Object... formatArgs) {
		android.util.Log.d(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void debug(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.d(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	public void info(final String messageFormat, final Object... formatArgs) {
		android.util.Log.i(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void info(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.i(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	public void warn(final String messageFormat, final Object... formatArgs) {
		android.util.Log.w(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void warn(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.w(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	public void error(final String messageFormat, final Object... formatArgs) {
		android.util.Log.e(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void error(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.e(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	public void wtf(final String messageFormat, final Object... formatArgs) {
		android.util.Log.wtf(m_tag, Log.format(messageFormat, formatArgs));
	}

	public void wtf(final String messageFormat, final Throwable exception, final Object... formatArgs) {
		android.util.Log.wtf(m_tag, Log.format(messageFormat, formatArgs), exception);
	}

	// #endregion
}
