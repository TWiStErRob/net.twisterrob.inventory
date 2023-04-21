package net.twisterrob.inventory.android.activity.space

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.simple.SimpleContext
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.slf4j.Logger

fun <STATE : Any, SIDE_EFFECT : Any>
	Container<STATE, SIDE_EFFECT>.decorateLogging(log: Logger): Container<STATE, SIDE_EFFECT> =
	LoggingContainerDecorator(this, log)

@OptIn(OrbitInternal::class)
private class LoggingContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
	override val actual: Container<STATE, SIDE_EFFECT>,
	private val log: Logger,
) : ContainerDecorator<STATE, SIDE_EFFECT> {

	override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
		super.orbit {
			logIntent(orbitIntent)
			this.logged().orbitIntent()
		}
	}

	override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
		super.inlineOrbit {
			logIntent(orbitIntent)
			this.logged().orbitIntent()
		}
	}

	private fun ContainerContext<STATE, SIDE_EFFECT>.logged(): ContainerContext<STATE, SIDE_EFFECT> =
		ContainerContext(
			settings = settings,
			postSideEffect = {
				logSideEffect(it)
				postSideEffect(it)
			},
			getState = ::state,
			reduce = { reducer ->
				reduce { oldState ->
					reducer(oldState).also { newState ->
						logReduction(reducer, oldState, newState)
					}
				}
			},
			subscribedCounter = subscribedCounter,
		)

	/**
	 * @see org.orbitmvi.orbit.syntax.simple.intent
	 * @see org.orbitmvi.orbit.syntax.simple.blockingIntent
	 */
	private fun <STATE : Any, SIDE_EFFECT : Any> logIntent(
		orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
	) {
		val realTransformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit =
			orbitIntent.captured("transformer")
		log.trace("Starting intent: {}", realTransformer.lambdaName())
	}

	private fun <SIDE_EFFECT : Any> logSideEffect(sideEffect: SIDE_EFFECT) {
		log.trace("postSideEffect({})", sideEffect)
	}

	/**
	 * @see org.orbitmvi.orbit.syntax.simple.reduce
	 */
	private fun <STATE : Any> logReduction(
		reducer: (STATE) -> STATE,
		oldState: STATE,
		newState: STATE
	) {
		val realReducer: SimpleContext<STATE>.() -> STATE =
			reducer.captured("reducer")
		log.trace("reduced via {}:\n{}\n->\n{}", realReducer.lambdaName(), oldState, newState)
	}
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
