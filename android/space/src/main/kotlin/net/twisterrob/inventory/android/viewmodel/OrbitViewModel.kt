package net.twisterrob.inventory.android.viewmodel

import net.twisterrob.inventory.android.arch.UiEffect
import net.twisterrob.inventory.android.arch.UiState
import net.twisterrob.inventory.android.content.VariantViewModel
import net.twisterrob.inventory.android.logger
import net.twisterrob.orbit.logging.decorateLogging
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

abstract class OrbitViewModel<State : UiState, Effect : UiEffect>(
	initialState: State
) : VariantViewModel(), OrbitContainerHost<State, State, Effect> {

	override val container = orbitContainer<State, Effect>(initialState)
		.decorateLogging(logger(this::class))
}
