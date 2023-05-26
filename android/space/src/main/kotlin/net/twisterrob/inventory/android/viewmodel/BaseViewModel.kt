package net.twisterrob.inventory.android.viewmodel

import net.twisterrob.inventory.android.content.VariantViewModel
import net.twisterrob.orbit.logging.decorateLogging
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import org.slf4j.LoggerFactory

abstract class BaseViewModel<State : Any, Effect : Any>(
	initialState: State
) : VariantViewModel(), ContainerHost<State, Effect> {

	override val container = container<State, Effect>(initialState)
		.decorateLogging(LoggerFactory.getLogger(this::class.java))
}

fun interface EffectHandler<Effect> {
	suspend fun handleEffect(effect: Effect)
}

fun interface Mapper<Input, Output> {
	fun map(input: Input): Output
}

fun interface UseCase<Input, Output> {
	suspend fun execute(input: Input): Output
}
