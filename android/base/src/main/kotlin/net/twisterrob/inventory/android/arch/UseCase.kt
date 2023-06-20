package net.twisterrob.inventory.android.arch

fun interface UseCase<Input, Output> {
	suspend fun execute(input: Input): Output
}
