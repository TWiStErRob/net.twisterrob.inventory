package net.twisterrob.inventory.android.arch

fun interface UiStateHandler<State : UiState> {
	fun render(state: State)
}
