package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@TargetApi(VERSION_CODES.HONEYCOMB)
@IntDef(value = {
		Cursor.FIELD_TYPE_NULL,
		Cursor.FIELD_TYPE_INTEGER,
		Cursor.FIELD_TYPE_FLOAT,
		Cursor.FIELD_TYPE_STRING,
		Cursor.FIELD_TYPE_BLOB,
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface CursorFieldType {
	class Converter {
		@DebugHelper
		public static String toString(@CursorFieldType int type) {
			switch (type) {
				case Cursor.FIELD_TYPE_NULL:
					return "FIELD_TYPE_NULL";
				case Cursor.FIELD_TYPE_INTEGER:
					return "FIELD_TYPE_INTEGER";
				case Cursor.FIELD_TYPE_FLOAT:
					return "FIELD_TYPE_FLOAT";
				case Cursor.FIELD_TYPE_STRING:
					return "FIELD_TYPE_STRING";
				case Cursor.FIELD_TYPE_BLOB:
					return "FIELD_TYPE_BLOB";
			}
			return "fieldType::" + type;
		}
	}
}
