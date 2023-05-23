package net.twisterrob.orbit.logging

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.simple.SimpleContext
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax

@OptIn(OrbitInternal::class)
class LoggingContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
	override val actual: Container<STATE, SIDE_EFFECT>,
	private val events: OrbitEvents<STATE, SIDE_EFFECT>,
) : ContainerDecorator<STATE, SIDE_EFFECT> {

	interface OrbitEvents<STATE : Any, SIDE_EFFECT : Any> {
		fun intentStarted(transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)
		fun intentFinished(transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)

		fun sideEffect(sideEffect: SIDE_EFFECT)

		fun reduce(oldState: STATE, reducer: SimpleContext<STATE>.() -> STATE, newState: STATE)
	}

	override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
		super.orbit {
			events.intentStarted(orbitIntent.captured("transformer"))
			this.logged().orbitIntent()
			events.intentFinished(orbitIntent.captured("transformer"))
		}
	}

	override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
		super.inlineOrbit {
			events.intentStarted(orbitIntent.captured("transformer"))
			this.logged().orbitIntent()
			events.intentFinished(orbitIntent.captured("transformer"))
		}
	}

	/**
	 * @see org.orbitmvi.orbit.syntax.simple.intent
	 * @see org.orbitmvi.orbit.syntax.simple.blockingIntent
	 * @see org.orbitmvi.orbit.syntax.simple.reduce
	 */
	private fun ContainerContext<STATE, SIDE_EFFECT>.logged(): ContainerContext<STATE, SIDE_EFFECT> =
		ContainerContext(
			settings = settings,
			postSideEffect = {
				events.sideEffect(it)
				postSideEffect(it)
			},
			getState = ::state,
			reduce = { reducer ->
				reduce { oldState ->
					reducer(oldState).also { newState ->
						events.reduce(oldState, reducer.captured("reducer"), newState)
					}
				}
			},
			subscribedCounter = subscribedCounter,
		)
}

/**
 * The framework's reducer and intent declarations are lambdas inside Orbit's functions.
 * We need to access the captured local variables to get our original reducer and intent lambdas.
 */
private fun <T : Function<*>> Function<*>.captured(localName: String): T =
	this::class
		.java
		.getDeclaredField("\$${localName}")
		.apply { isAccessible = true }
		.get(this)
		.let { @Suppress("UNCHECKED_CAST") (it as T) }
