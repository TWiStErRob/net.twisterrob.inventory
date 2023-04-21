package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.simple.SimpleContext
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.viewmodel.container
import org.slf4j.LoggerFactory
import org.orbitmvi.orbit.syntax.simple.intent as orbitIntent

abstract class BaseViewModel<State : Any, Effect : Any>(
	initialState: State
) : ViewModel(), ContainerHost<State, Effect> {

	private val LOG = LoggerFactory.getLogger(this::class.java)

	override val container = container<State, Effect>(initialState)

	@OrbitDsl
	fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.intent(
		registerIdling: Boolean = true,
		transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
	) {
		logIntent(transformer)
		this@intent.orbitIntent(registerIdling) {
			@OptIn(OrbitInternal::class)
			SimpleSyntax(
				ContainerContext<STATE, SIDE_EFFECT>(
					settings = containerContext.settings,
					postSideEffect = {
						logSideEffect(it)
						containerContext.postSideEffect(it)
					},
					getState = containerContext::state,
					reduce = { reducer ->
						containerContext.reduce { oldState ->
							reducer(oldState).also { newState ->
								logReduction(reducer, oldState, newState)
							}
						}
					},
					subscribedCounter = containerContext.subscribedCounter,
				)
			).transformer()
		}
	}

	private fun <STATE : Any, SIDE_EFFECT : Any> logIntent(
		transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
	) {
		LOG.trace("Starting intent: {}", transformer.lambdaName())
	}

	private fun <SIDE_EFFECT : Any> logSideEffect(sideEffect: SIDE_EFFECT) {
		LOG.trace("postSideEffect({})", sideEffect)
	}

	private fun <STATE : Any> logReduction(
		reducer: (STATE) -> STATE,
		oldState: STATE,
		newState: STATE
	) {
		// reducer here is the lambda inside Orbit's `reduce`.
		// We need to access the captured local variable `reducer` to get our original reducer.
		val realReducer = reducer::class
			.java
			.getDeclaredField("\$reducer")
			.apply { isAccessible = true }
			.get(reducer)
			.let { @Suppress("UNCHECKED_CAST") (it as (SimpleContext<STATE>.() -> STATE)) }
		LOG.trace("reduced via {}:\n{}\n->\n{}", realReducer.lambdaName(), oldState, newState)
	}

	/**
	 * An instantiated (not inlined) lambda is always an instance of Function,
	 * it'll have the enclosing class AND method name as the class name.
	 */
	private fun Function<*>.lambdaName(): String = this::class.java.name
		// Lambdas (anonymous classes) don't have .simpleName,
		// so need to resort to string manipulation to remove the package prefix.
		.replaceBeforeLast('.', "")
		// Remove the last dot too.
		.removePrefix(".")
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
