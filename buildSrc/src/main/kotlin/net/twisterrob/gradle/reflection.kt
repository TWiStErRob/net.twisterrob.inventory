package net.twisterrob.gradle

import java.lang.reflect.Field

fun <T> Field.getValue(obj: Any): T = run {
	val accessible = isAccessible
	try {
		isAccessible = true
		@Suppress("UNCHECKED_CAST")
		get(obj) as T
	} finally {
		isAccessible = accessible
	}
}

fun Field.setValue(obj: Any, value: Any?): Any? = run {
	val accessible = isAccessible
	try {
		isAccessible = true
		set(obj, value)
	} finally {
		isAccessible = accessible
	}
}
