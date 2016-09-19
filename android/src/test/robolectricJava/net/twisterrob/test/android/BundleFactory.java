package net.twisterrob.test.android;

import java.io.Serializable;
import java.util.*;

import android.annotation.TargetApi;
import android.os.Build.*;
import android.os.*;
import android.util.*;

public class BundleFactory {
	public static Bundle allTypes() {
		Bundle b = new Bundle();
		b.putAll(allPrimitives());
		b.putAll(allArrays());
		b.putAll(allStrings());
		b.putAll(allCollections());
		b.putAll(objects());
		return b;
	}

	public static Bundle allArrays() {
		Bundle b = new Bundle();
		b.putAll(primitiveArrays());
		b.putAll(stringArrays());
		b.putAll(otherArrays());
		return b;
	}
	public static Bundle allCollections() {
		Bundle b = new Bundle();
		b.putAll(stringLists());
		b.putAll(otherLists());
		return b;
	}

	public static Bundle allStrings() {
		Bundle b = new Bundle();
		b.putAll(strings());
		b.putAll(stringArrays());
		b.putAll(stringLists());
		return b;
	}

	public static Bundle allPrimitives() {
		Bundle b = new Bundle();
		b.putAll(primitives());
		b.putAll(primitiveArrays());
		return b;
	}

	public static Bundle primitives() {
		Bundle b = new Bundle();
		b.putBoolean("Boolean", true);
		b.putByte("Byte", Byte.MAX_VALUE);
		b.putChar("Char", '\uffff');
		b.putDouble("Double", Double.MAX_VALUE);
		b.putFloat("Float", Float.MAX_VALUE);
		b.putInt("Int", Integer.MAX_VALUE);
		b.putLong("Long", Long.MAX_VALUE);
		b.putShort("Short", Short.MAX_VALUE);
		return b;
	}

	public static Bundle primitiveArrays() {
		Bundle b = new Bundle();
		b.putBooleanArray("BooleanArray", new boolean[] {true, false, true});
		b.putByteArray("ByteArray", new byte[] {1, 2, 3});
		b.putCharArray("CharArray", new char[] {'a', 'b', 'c'});
		b.putDoubleArray("DoubleArray", new double[] {1, 2, 3});
		b.putFloatArray("FloatArray", new float[] {1, 2, 3});
		b.putIntArray("IntArray", new int[] {1, 2, 3});
		b.putLongArray("LongArray", new long[] {1, 2, 3});
		b.putShortArray("ShortArray", new short[] {1, 2, 3});
		return b;
	}

	public static Bundle otherLists() {
		Bundle b = new Bundle();
		b.putIntegerArrayList("IntegerArrayList", new ArrayList<>(Arrays.asList(1, 2, 3)));
		b.putParcelableArrayList("ParcelableArrayList", BundleFactory.<ArrayList<Parcelable>>TODO());
		b.putSparseParcelableArray("SparseParcelableArray", BundleFactory.<SparseArray<Parcelable>>TODO());
		return b;
	}
	public static Bundle otherArrays() {
		Bundle b = new Bundle();
		b.putParcelableArray("ParcelableArray", BundleFactory.<Parcelable[]>TODO());
		return b;
	}
	public static Bundle stringArrays() {
		Bundle b = new Bundle();
		b.putCharSequenceArray("CharSequenceArray", new CharSequence[] {"chars1", "chars2"});
		b.putStringArray("StringArray", new String[] {"string1", "string2"});
		return b;
	}
	public static Bundle stringLists() {
		Bundle b = new Bundle();
		b.putCharSequenceArrayList("CharSequenceArrayList",
				new ArrayList<>(Arrays.<CharSequence>asList("chars1", "chars2")));
		b.putStringArrayList("StringArrayList", new ArrayList<>(Arrays.asList("string1", "string2")));
		return b;
	}

	public static Bundle strings() {
		Bundle b = new Bundle();
		b.putCharSequence("CharSequence", "string");
		b.putString("String", "string");
		return b;
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static Bundle objects() {
		Bundle b = new Bundle();
		if (VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT) {
			b.putSize("Size", new Size(1, 2));
			b.putSizeF("SizeF", new SizeF(1, 2));
		}

		if (VERSION_CODES.JELLY_BEAN_MR2 <= VERSION.SDK_INT) {
			b.putBinder("Binder", BundleFactory.<IBinder>TODO());
		}
		b.putSerializable("Serializable", BundleFactory.<Serializable>TODO());
		b.putParcelable("Parcelable", BundleFactory.<Parcelable>TODO());

		b.putBundle("Bundle", new Bundle());
		return b;
	}
	private static <T> T TODO() {
		return null;
	}
}
