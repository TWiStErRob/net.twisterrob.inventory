/*
 * Created 21.10.2009
 * Modified 26.10.2013
 *
 * Copyright (c) 2009 SLF4J.ORG
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.impl;

import org.slf4j.Marker;
import org.slf4j.helpers.*;

import android.util.Log;

/**
 * A simple implementation that delegates all log requests to the Google Android
 * logging facilities. Note that this logger does not support {@link Marker}.
 * That is, methods taking marker data simply invoke the corresponding method
 * without the Marker argument, discarding any marker data passed as argument.
 * <p>
 * The logging levels specified for SLF4J can be almost directly mapped to
 * the levels that exist in the Google Android platform. The following table
 * shows the mapping implemented by this logger.
 * <p>
 * <table border="1">
 * 	<tr><th><b>SLF4J<b></th><th><b>Android</b></th></tr>
 * 	<tr><td>TRACE</td><td>{@link Log#VERBOSE}</td></tr>
 * 	<tr><td>DEBUG</td><td>{@link Log#DEBUG}</td></tr>
 * 	<tr><td>INFO</td><td>{@link Log#INFO}</td></tr>
 * 	<tr><td>WARN</td><td>{@link Log#WARN}</td></tr>
 * 	<tr><td>ERROR</td><td>{@link Log#ERROR}</td></tr>
 * </table>
 * 
 * Note: this class has been modified to incorporate the 1.7.x features, like varargs and throwable as last argument.
 *
 * @author Thorsten M&ouml;ller
 * @author papp.robert.s@gmail.com
 * @version $Rev:$; $Author:$; $Date:$
 */
public class AndroidLogger extends MarkerIgnoringBase {
	private static final long serialVersionUID = -1227274521521287937L;

	/**
	 * Package access allows only {@link AndroidLoggerFactory} to instantiate
	 * SimpleLogger instances.
	 */
	AndroidLogger(final String name) {
		this.name = name;
	}

	public boolean isTraceEnabled() {
		return Log.isLoggable(name, Log.VERBOSE);
	}

	public void trace(final String msg) {
		Log.v(name, msg);
	}

	public void trace(final String format, final Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.v(name, ft.getMessage(), ft.getThrowable());
	}

	public void trace(final String format, final Object arg1, final Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.v(name, ft.getMessage(), ft.getThrowable());
	}

	public void trace(final String format, final Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.v(name, ft.getMessage(), ft.getThrowable());
	}

	public void trace(final String msg, final Throwable t) {
		Log.v(name, msg, t);
	}

	public boolean isDebugEnabled() {
		return Log.isLoggable(name, Log.DEBUG);
	}

	public void debug(final String msg) {
		Log.d(name, msg);
	}

	public void debug(final String format, final Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.d(name, ft.getMessage(), ft.getThrowable());
	}

	public void debug(final String format, final Object arg1, final Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.d(name, ft.getMessage(), ft.getThrowable());
	}

	public void debug(final String format, final Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.d(name, ft.getMessage(), ft.getThrowable());
	}

	public void debug(final String msg, final Throwable t) {
		Log.d(name, msg, t);
	}

	public boolean isInfoEnabled() {
		return Log.isLoggable(name, Log.INFO);
	}

	public void info(final String msg) {
		Log.i(name, msg);
	}

	public void info(final String format, final Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.i(name, ft.getMessage(), ft.getThrowable());
	}

	public void info(final String format, final Object arg1, final Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.i(name, ft.getMessage(), ft.getThrowable());
	}

	public void info(final String format, final Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.i(name, ft.getMessage(), ft.getThrowable());
	}

	public void info(final String msg, final Throwable t) {
		Log.i(name, msg, t);
	}

	public boolean isWarnEnabled() {
		return Log.isLoggable(name, Log.WARN);
	}

	public void warn(final String msg) {
		Log.w(name, msg);
	}

	public void warn(final String format, final Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.w(name, ft.getMessage(), ft.getThrowable());
	}

	public void warn(final String format, final Object arg1, final Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.w(name, ft.getMessage(), ft.getThrowable());
	}

	public void warn(final String format, final Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.w(name, ft.getMessage(), ft.getThrowable());
	}

	public void warn(final String msg, final Throwable t) {
		Log.w(name, msg, t);
	}

	public boolean isErrorEnabled() {
		return Log.isLoggable(name, Log.ERROR);
	}

	public void error(final String msg) {
		Log.e(name, msg);
	}

	public void error(final String format, final Object arg) {
		FormattingTuple ft = MessageFormatter.format(format, arg);
		Log.e(name, ft.getMessage(), ft.getThrowable());
	}

	public void error(final String format, final Object arg1, final Object arg2) {
		FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
		Log.e(name, ft.getMessage(), ft.getThrowable());
	}

	public void error(final String format, final Object... argArray) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
		Log.e(name, ft.getMessage(), ft.getThrowable());
	}

	public void error(final String msg, final Throwable t) {
		Log.e(name, msg, t);
	}
}
