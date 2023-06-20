package net.twisterrob.inventory.android.arch

fun interface Mapper<Input, Output> {
	fun map(input: Input): Output
}
