package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.ViewModel
import net.twisterrob.orbit.logging.OrbitSlf4jLogger.Companion.decorateLogging
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import org.slf4j.LoggerFactory

abstract class BaseViewModel<State : Any, Effect : Any>(
	initialState: State
) : ViewModel(), ContainerHost<State, Effect> {

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
