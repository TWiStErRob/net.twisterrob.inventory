package net.twisterrob.android.utils.tools;

import java.util.*;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.SparseArray;

import net.twisterrob.android.annotation.CursorFieldType;

@SuppressLint("InlinedApi")
public enum CursorColumnType {
	String(Cursor.FIELD_TYPE_STRING) {
		@Override public String getValue(Cursor cursor, int columnIndex) {
			return cursor.getString(columnIndex);
		}
	},
	Short(Cursor.FIELD_TYPE_INTEGER) {
		@Override protected Short getPrimitiveValue(Cursor cursor, int columnIndex) {
			return cursor.getShort(columnIndex);
		}
	},
	Int(Cursor.FIELD_TYPE_INTEGER) {
		@Override protected Integer getPrimitiveValue(Cursor cursor, int columnIndex) {
			return cursor.getInt(columnIndex);
		}
	},
	Long(Cursor.FIELD_TYPE_INTEGER) {
		@Override protected Long getPrimitiveValue(Cursor cursor, int columnIndex) {
			return cursor.getLong(columnIndex);
		}
	},
	Float(Cursor.FIELD_TYPE_FLOAT) {
		@Override protected Float getPrimitiveValue(Cursor cursor, int columnIndex) {
			return cursor.getFloat(columnIndex);
		}
	},
	Double(Cursor.FIELD_TYPE_FLOAT) {
		@Override protected Double getPrimitiveValue(Cursor cursor, int columnIndex) {
			return cursor.getDouble(columnIndex);
		}
	},
	Blob(Cursor.FIELD_TYPE_BLOB) {
		@Override public byte[] getValue(Cursor cursor, int columnIndex) {
			return cursor.getBlob(columnIndex);
		}
	},
	Null(Cursor.FIELD_TYPE_NULL);

	private final @CursorFieldType int fieldType;
	CursorColumnType(@CursorFieldType int fieldType) {
		this.fieldType = fieldType;
	}
	public @CursorFieldType int getFieldType() {
		return fieldType;
	}

	public Object getValue(Cursor cursor, String columnName) {
		return getValue(cursor, cursor.getColumnIndexOrThrow(columnName));
	}
	public Object getValue(Cursor cursor, int columnIndex) {
		return cursor.isNull(columnIndex)? null : getPrimitiveValue(cursor, columnIndex);
	}
	protected Object getPrimitiveValue(Cursor cursor, int columnIndex) {
		throw new UnsupportedOperationException("Cannot get primitive value for " + this);
	}

	public static CursorColumnType fromClass(Class<?> clazz) {
		return COLUMN_CLASSES.get(clazz);
	}
	public static CursorColumnType fromFieldType(@CursorFieldType int fieldType) {
		CursorColumnType type = FIELD_TYPES.get(fieldType);
		if (type == null) {
			throw new IllegalArgumentException(
					"Invalid field type: " + CursorFieldType.Converter.toString(fieldType));
		}
		return type;
	}
	private static final SparseArray<CursorColumnType> FIELD_TYPES = new SparseArray<>();

	static {
		FIELD_TYPES.put(Cursor.FIELD_TYPE_NULL, Null);
		FIELD_TYPES.put(Cursor.FIELD_TYPE_INTEGER, Long);
		FIELD_TYPES.put(Cursor.FIELD_TYPE_FLOAT, Double);
		FIELD_TYPES.put(Cursor.FIELD_TYPE_STRING, String);
		FIELD_TYPES.put(Cursor.FIELD_TYPE_BLOB, Blob);
	}

	private static final Map<Class<?>, CursorColumnType> COLUMN_CLASSES = new HashMap<>();

	static {
		COLUMN_CLASSES.put(java.lang.String.class, String);
		COLUMN_CLASSES.put(byte[].class, Blob);
		COLUMN_CLASSES.put(java.lang.Integer.class, Int);
		COLUMN_CLASSES.put(java.lang.Integer.TYPE, Int);
		COLUMN_CLASSES.put(java.lang.Long.class, Long);
		COLUMN_CLASSES.put(java.lang.Long.TYPE, Long);
		COLUMN_CLASSES.put(java.lang.Short.class, Short);
		COLUMN_CLASSES.put(java.lang.Short.TYPE, Short);
		COLUMN_CLASSES.put(java.lang.Float.class, Float);
		COLUMN_CLASSES.put(java.lang.Float.TYPE, Float);
		COLUMN_CLASSES.put(java.lang.Double.class, Double);
		COLUMN_CLASSES.put(java.lang.Double.TYPE, Double);
	}
}
