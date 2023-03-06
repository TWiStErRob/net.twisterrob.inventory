package net.twisterrob.inventory.build.tests

import java.lang.reflect.Field

fun <T> Field.getValue(obj: Any): T {
	val accessible = @Suppress("DEPRECATION") isAccessible
	try {
		isAccessible = true
		@Suppress("UNCHECKED_CAST")
		return get(obj) as T
	} finally {
		isAccessible = accessible
	}
}

fun Field.setValue(obj: Any, value: Any?) {
	val accessible = @Suppress("DEPRECATION") isAccessible
	try {
		isAccessible = true
		set(obj, value)
	} finally {
		isAccessible = accessible
	}
}
