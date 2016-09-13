package net.twisterrob.java.utils.tostring;

import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class DefaultStringerTest_ToString {
	/** Default toString's pattern without class name. */
	public static final Pattern OBJECT_TOSTRING = Pattern.compile("^@[0-9a-f]+$");
	public static final Pattern SQUARE_BRACKETS = Pattern.compile("^\\[.*\\]$");
	private final DefaultStringer stringer = new DefaultStringer();

	@Test public void testNull() {
		checkToStringOf(null, is("null"));
	}

	@Test public void testPrimitives() {
		checkToStringOf(0, is("0"));
		checkToStringOf(0L, is("0"));
		checkToStringOf(0.0f, is("0.0"));
		checkToStringOf(0.0d, is("0.0"));
		checkToStringOf((short)0, is("0"));
		checkToStringOf((byte)0, is("0"));
		checkToStringOf('0', is("0"));
		checkToStringOf(false, is("false"));
		checkToStringOf(true, is("true"));
	}

	@Test public void testPrimitiveArrayEmpty() {
		checkToStringOf(new int[0], is("[]"));
		checkToStringOf(new long[0], is("[]"));
		checkToStringOf(new float[0], is("[]"));
		checkToStringOf(new double[0], is("[]"));
		checkToStringOf(new short[0], is("[]"));
		checkToStringOf(new byte[0], is("[]"));
		checkToStringOf(new char[0], is("[]"));
		checkToStringOf(new boolean[0], is("[]"));
	}
	@Test public void testPrimitiveArray() {
		checkToStringOf(new int[1], is("[0]"));
		checkToStringOf(new long[1], is("[0]"));
		checkToStringOf(new float[1], is("[0.0]"));
		checkToStringOf(new double[1], is("[0.0]"));
		checkToStringOf(new short[1], is("[0]"));
		checkToStringOf(new byte[1], is("[0]"));
		checkToStringOf(new char[1], is("[\0]"));
		checkToStringOf(new boolean[1], is("[false]"));
	}

	@Test public void testWrapperArrayEmpty() {
		checkToStringOf(new Integer[0], is("[]"));
		checkToStringOf(new Long[0], is("[]"));
		checkToStringOf(new Float[0], is("[]"));
		checkToStringOf(new Double[0], is("[]"));
		checkToStringOf(new Short[0], is("[]"));
		checkToStringOf(new Byte[0], is("[]"));
		checkToStringOf(new Character[0], is("[]"));
		checkToStringOf(new Boolean[0], is("[]"));
	}
	@Test public void testWrapperArray() {
		checkToStringOf(new Integer[1], is("[null]"));
		checkToStringOf(new Long[1], is("[null]"));
		checkToStringOf(new Float[1], is("[null]"));
		checkToStringOf(new Double[1], is("[null]"));
		checkToStringOf(new Short[1], is("[null]"));
		checkToStringOf(new Byte[1], is("[null]"));
		checkToStringOf(new Character[1], is("[null]"));
		checkToStringOf(new Boolean[1], is("[null]"));
	}

	@Test public void testPrimitiveArrayMultiEmpty() {
		checkToStringOf(new int[0][0][0], is("[]"));
		checkToStringOf(new long[0][0][0], is("[]"));
		checkToStringOf(new float[0][0][0], is("[]"));
		checkToStringOf(new double[0][0][0], is("[]"));
		checkToStringOf(new short[0][0][0], is("[]"));
		checkToStringOf(new byte[0][0][0], is("[]"));
		checkToStringOf(new char[0][0][0], is("[]"));
		checkToStringOf(new boolean[0][0][0], is("[]"));
	}

	@Test public void testPrimitiveArrayMulti() {
		checkToStringOf(new int[2][1], is("[[0], [0]]"));
		checkToStringOf(new long[2][1], is("[[0], [0]]"));
		checkToStringOf(new float[2][1], is("[[0.0], [0.0]]"));
		checkToStringOf(new double[2][1], is("[[0.0], [0.0]]"));
		checkToStringOf(new short[2][1], is("[[0], [0]]"));
		checkToStringOf(new byte[2][1], is("[[0], [0]]"));
		checkToStringOf(new char[2][1], is("[[\0], [\0]]"));
		checkToStringOf(new boolean[2][1], is("[[false], [false]]"));
	}

	@Test public void testWrapperArrayMultiEmpty() {
		checkToStringOf(new Integer[0][0][0], is("[]"));
		checkToStringOf(new Long[0][0][0], is("[]"));
		checkToStringOf(new Float[0][0][0], is("[]"));
		checkToStringOf(new Double[0][0][0], is("[]"));
		checkToStringOf(new Short[0][0][0], is("[]"));
		checkToStringOf(new Byte[0][0][0], is("[]"));
		checkToStringOf(new Character[0][0][0], is("[]"));
		checkToStringOf(new Boolean[0][0][0], is("[]"));
	}

	@Test public void testWrapperArrayMulti() {
		checkToStringOf(new Integer[2][1], is("[[null], [null]]"));
		checkToStringOf(new Long[2][1], is("[[null], [null]]"));
		checkToStringOf(new Float[2][1], is("[[null], [null]]"));
		checkToStringOf(new Double[2][1], is("[[null], [null]]"));
		checkToStringOf(new Short[2][1], is("[[null], [null]]"));
		checkToStringOf(new Byte[2][1], is("[[null], [null]]"));
		checkToStringOf(new Character[2][1], is("[[null], [null]]"));
		checkToStringOf(new Boolean[2][1], is("[[null], [null]]"));
	}

	@Test public void testPackageJavaLang() {
		checkToStringOf("", is(""));
		checkToStringOf(new Exception("test"), is("test"));
		checkToStringOf(new Object(), matchesPattern(OBJECT_TOSTRING));
		checkToStringOf(Runtime.getRuntime(), matchesPattern(OBJECT_TOSTRING));
		checkToStringOf(Thread.currentThread(), matchesPattern(SQUARE_BRACKETS));
	}

	@Test public void testPackageJava() {
		checkToStringOf(new java.util.ArrayList<>(), is("[]"));
		checkToStringOf(new java.util.concurrent.atomic.AtomicReference<>(), is("null"));
		checkToStringOf(new java.io.IOException("test"), is("test"));
	}

	@Test public void testPackageJavaX() {
		checkToStringOf(new javax.naming.NameNotFoundException("test"), is("test"));
		checkToStringOf(new javax.imageio.ImageReadParam(), matchesPattern(OBJECT_TOSTRING));
		checkToStringOf(new javax.swing.JButton(), matchesPattern(SQUARE_BRACKETS));
	}

	@Test public void testPackageMine() {
		checkToStringOf(stringer, matchesPattern(OBJECT_TOSTRING));
	}

	@Test public void testPackageOther() {
		checkToStringOf(new org.junit.AssumptionViolatedException("test"), is("test"));
		checkToStringOf(org.hamcrest.core.IsAnything.anything(), is(anything().toString()));
	}

	private void checkToStringOf(Object value, Matcher<? super String> matcher) {
		ToStringAppender appender = Mockito.mock(ToStringAppender.class);

		stringer.toString(appender, value);

		ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
		Mockito.verify(appender).selfDescribingProperty(capture.capture());
		String toString = capture.getValue();

		assertThat(toString, matcher);

		Mockito.verifyNoMoreInteractions(appender);
	}
}
