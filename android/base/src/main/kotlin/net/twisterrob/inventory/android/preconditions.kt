package net.twisterrob.inventory.android

fun check(value: Boolean) {
	kotlin.check(value)
}

fun require(value: Boolean) {
	kotlin.require(value)
}

fun <T> checkNotNull(value: T?): T =
	kotlin.checkNotNull(value)

fun <T> requireNotNull(value: T?): T =
	kotlin.requireNotNull(value)
