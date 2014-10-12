package org.slf4j.impl;

import org.slf4j.Marker;
import org.slf4j.helpers.*;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * A simple implementation that delegates all log requests to the Google Android logging facilities.
 * Note that this logger does not support {@link Marker}. That is, methods taking marker data
 * simply invoke the corresponding method without the Marker argument, discarding any marker data passed as argument.
 *
 * <p>
 * Logging levels specified for SLF4J can be almost directly mapped to the levels that exist in the Android platform.
 * The following table shows the mapping implemented by this logger:
 * </p>
 * <table border="1">
 *  <tr><th><b>SLF4J<b></th><th><b>Android</b></th></tr>
 *  <tr><td>TRACE</td><td>{@link Log#VERBOSE}</td></tr>
 *  <tr><td>DEBUG</td><td>{@link Log#DEBUG}</td></tr>
 *  <tr><td>INFO</td><td>{@link Log#INFO}</td></tr>
 *  <tr><td>WARN</td><td>{@link Log#WARN}</td></tr>
 *  <tr><td>ERROR</td><td>{@link Log#ERROR}</td></tr>
 * </table>
 *
 * @author papp.robert.s@gmail.com
 */
public class AndroidLogger extends MarkerIgnoringBase {
	private static final long serialVersionUID = -1227274521521287937L;
	private final String tag;
	private final String originalName;

	/**
	 * Package access allows only {@link AndroidLoggerFactory} to instantiate AndroidLogger instances.
	 */
	AndroidLogger(@NonNull String originalName, @NonNull String name, @NonNull String tag) {
		super.name = name;
		this.originalName = originalName;
		this.tag = tag;
	}

	public @NonNull String getOriginalName() {
		return originalName;
	}

	public boolean isTraceEnabled() {
		return Log.isLoggable(tag, Log.VERBOSE);
	}

	public void trace(String msg) {
		Log.v(tag, msg);
	}

	public void trace(String format, Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.v(tag, ft.getMessage(), ft.getThrowable());
	}

	public void trace(String format, Object arg1, Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.v(tag, ft.getMessage(), ft.getThrowable());
	}

	public void trace(String format, Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.v(tag, ft.getMessage(), ft.getThrowable());
	}

	public void trace(String msg, Throwable t) {
		Log.v(tag, msg, t);
	}

	public boolean isDebugEnabled() {
		return Log.isLoggable(tag, Log.DEBUG);
	}

	public void debug(String msg) {
		Log.d(tag, msg);
	}

	public void debug(String format, Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.d(tag, ft.getMessage(), ft.getThrowable());
	}

	public void debug(String format, Object arg1, Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.d(tag, ft.getMessage(), ft.getThrowable());
	}

	public void debug(String format, Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.d(tag, ft.getMessage(), ft.getThrowable());
	}

	public void debug(String msg, Throwable t) {
		Log.d(tag, msg, t);
	}

	public boolean isInfoEnabled() {
		return Log.isLoggable(tag, Log.INFO);
	}

	public void info(String msg) {
		Log.i(tag, msg);
	}

	public void info(String format, Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.i(tag, ft.getMessage(), ft.getThrowable());
	}

	public void info(String format, Object arg1, Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.i(tag, ft.getMessage(), ft.getThrowable());
	}

	public void info(String format, Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.i(tag, ft.getMessage(), ft.getThrowable());
	}

	public void info(String msg, Throwable t) {
		Log.i(tag, msg, t);
	}

	public boolean isWarnEnabled() {
		return Log.isLoggable(tag, Log.WARN);
	}

	public void warn(String msg) {
		Log.w(tag, msg);
	}

	public void warn(String format, Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.w(tag, ft.getMessage(), ft.getThrowable());
	}

	public void warn(String format, Object arg1, Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.w(tag, ft.getMessage(), ft.getThrowable());
	}

	public void warn(String format, Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.w(tag, ft.getMessage(), ft.getThrowable());
	}

	public void warn(String msg, Throwable t) {
		Log.w(tag, msg, t);
	}

	public boolean isErrorEnabled() {
		return Log.isLoggable(tag, Log.ERROR);
	}

	public void error(String msg) {
		Log.e(tag, msg);
	}

	public void error(String format, Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.e(tag, ft.getMessage(), ft.getThrowable());
	}

	public void error(String format, Object arg1, Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.e(tag, ft.getMessage(), ft.getThrowable());
	}

	public void error(String format, Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.e(tag, ft.getMessage(), ft.getThrowable());
	}

	public void error(String msg, Throwable t) {
		Log.e(tag, msg, t);
	}
}
