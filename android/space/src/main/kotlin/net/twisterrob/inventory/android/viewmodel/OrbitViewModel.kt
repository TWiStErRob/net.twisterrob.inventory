package net.twisterrob.inventory.android.viewmodel

import net.twisterrob.inventory.android.arch.UiEffect
import net.twisterrob.inventory.android.arch.UiState
import net.twisterrob.inventory.android.content.VariantViewModel
import net.twisterrob.inventory.android.logger
import net.twisterrob.orbit.logging.decorateLogging
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

abstract class OrbitViewModel<State : UiState, Effect : UiEffect>(
	initialState: State
) : VariantViewModel(), ContainerHost<State, Effect> {

	override val container = container<State, Effect>(initialState)
		.decorateLogging(logger(this::class))
}
