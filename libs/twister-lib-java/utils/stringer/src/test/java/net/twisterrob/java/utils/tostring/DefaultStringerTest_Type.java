package net.twisterrob.java.utils.tostring;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class DefaultStringerTest_Type {
	private final DefaultStringer stringer = new DefaultStringer();

	@Test public void testNull() {
		assertThat(stringer.getType(null), is("null"));
	}

	/** Currently all wrapped types (below are autoboxed) are shown as primitives, except for arrays. */
	@Test public void testPrimitives() {
		assertThat(stringer.getType(0), is("int"));
		assertThat(stringer.getType(0L), is("long"));
		assertThat(stringer.getType(0.0f), is("float"));
		assertThat(stringer.getType(0.0d), is("double"));
		assertThat(stringer.getType((short)0), is("short"));
		assertThat(stringer.getType((byte)0), is("byte"));
		assertThat(stringer.getType('0'), is("char"));
		assertThat(stringer.getType(false), is("boolean"));
	}

	@Test public void testPackageJavaLang() {
		assertThat(stringer.getType(""), is("String"));
		assertThat(stringer.getType(new java.lang.Exception()), is("Exception"));
		assertThat(stringer.getType(new java.lang.Object()), is("Object"));
		assertThat(stringer.getType(java.lang.Runtime.getRuntime()), is("Runtime"));
		assertThat(stringer.getType(java.lang.Thread.currentThread()), is("Thread"));
	}

	@Test public void testPackageJava() {
		assertThat(stringer.getType(new java.util.ArrayList<>()), is("ArrayList"));
		assertThat(stringer.getType(new java.util.concurrent.atomic.AtomicReference<>()), is("AtomicReference"));
		assertThat(stringer.getType(new java.io.IOException()), is("IOException"));
	}

	@Test public void testPackageJavaX() {
		assertThat(stringer.getType(new javax.naming.NameNotFoundException()), is("jx.NameNotFoundException"));
		assertThat(stringer.getType(new javax.imageio.ImageReadParam()), is("jx.ImageReadParam"));
		assertThat(stringer.getType(new javax.swing.JButton()), is("swing.JButton"));
	}

	@Test public void testPackageMine() {
		assertThat(stringer.getType(stringer), is("tws.DefaultStringer"));
	}

	@Test public void testPackageOther() {
		assertThat(stringer.getType(new org.junit.AssumptionViolatedException("test")),
				is("org.junit.AssumptionViolatedException"));
		assertThat(stringer.getType(org.hamcrest.core.IsAnything.anything()),
				is("org.hamcrest.core.IsAnything"));
	}

	@Test public void testArray() {
		assertThat(stringer.getType(new int[1]), is("int[1]"));
		assertThat(stringer.getType(new long[1]), is("long[1]"));
		assertThat(stringer.getType(new float[1]), is("float[1]"));
		assertThat(stringer.getType(new double[1]), is("double[1]"));
		assertThat(stringer.getType(new short[1]), is("short[1]"));
		assertThat(stringer.getType(new byte[1]), is("byte[1]"));
		assertThat(stringer.getType(new char[1]), is("char[1]"));
		assertThat(stringer.getType(new boolean[1]), is("boolean[1]"));
	}

	@Test public void testWrapperArray() {
		assertThat(stringer.getType(new Integer[1]), is("Integer[1]"));
		assertThat(stringer.getType(new Long[1]), is("Long[1]"));
		assertThat(stringer.getType(new Float[1]), is("Float[1]"));
		assertThat(stringer.getType(new Double[1]), is("Double[1]"));
		assertThat(stringer.getType(new Short[1]), is("Short[1]"));
		assertThat(stringer.getType(new Byte[1]), is("Byte[1]"));
		assertThat(stringer.getType(new Character[1]), is("Character[1]"));
		assertThat(stringer.getType(new Boolean[1]), is("Boolean[1]"));
	}

	@Test public void testArrayMulti() {
		assertThat(stringer.getType(new int[3][2][1]), is("int[3][][]"));
		assertThat(stringer.getType(new long[3][2][1]), is("long[3][][]"));
		assertThat(stringer.getType(new float[3][2][1]), is("float[3][][]"));
		assertThat(stringer.getType(new double[3][2][1]), is("double[3][][]"));
		assertThat(stringer.getType(new short[3][2][1]), is("short[3][][]"));
		assertThat(stringer.getType(new byte[3][2][1]), is("byte[3][][]"));
		assertThat(stringer.getType(new char[3][2][1]), is("char[3][][]"));
		assertThat(stringer.getType(new boolean[3][2][1]), is("boolean[3][][]"));
	}

	@Test public void testWrapperArrayMulti() {
		assertThat(stringer.getType(new Integer[3][2][1]), is("Integer[3][][]"));
		assertThat(stringer.getType(new Long[3][2][1]), is("Long[3][][]"));
		assertThat(stringer.getType(new Float[3][2][1]), is("Float[3][][]"));
		assertThat(stringer.getType(new Double[3][2][1]), is("Double[3][][]"));
		assertThat(stringer.getType(new Short[3][2][1]), is("Short[3][][]"));
		assertThat(stringer.getType(new Byte[3][2][1]), is("Byte[3][][]"));
		assertThat(stringer.getType(new Character[3][2][1]), is("Character[3][][]"));
		assertThat(stringer.getType(new Boolean[3][2][1]), is("Boolean[3][][]"));
	}

	@Test public void testArrayDimensions() {
		assertThat(stringer.getType(new Object[0]), is("Object[0]"));
		assertThat(stringer.getType(new Object[1]), is("Object[1]"));
		assertThat(stringer.getType(new Object[2]), is("Object[2]"));
		assertThat(stringer.getType(new Object[3]), is("Object[3]"));
		assertThat(stringer.getType(new Object[4]), is("Object[4]"));
		assertThat(stringer.getType(new Object[2][3]), is("Object[2][]"));
		assertThat(stringer.getType(new Object[3][2]), is("Object[3][]"));
		assertThat(stringer.getType(new Object[4][]), is("Object[4][]"));
		assertThat(stringer.getType(new Object[5][][]), is("Object[5][][]"));
	}

	@Test public void testPackageArray() {
		assertThat(stringer.getType(new java.lang.String[0]), is("String[0]"));
		assertThat(stringer.getType(new java.util.concurrent.atomic.AtomicInteger[0]), is("AtomicInteger[0]"));
		assertThat(stringer.getType(new javax.swing.JButton[0]), is("swing.JButton[0]"));
		assertThat(stringer.getType(new DefaultStringer[0]), is("tws.DefaultStringer[0]"));
		assertThat(stringer.getType(new org.junit.AssumptionViolatedException[0]),
				is("org.junit.AssumptionViolatedException[0]"));
	}
}
