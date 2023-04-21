package net.twisterrob.orbit.logging

import net.twisterrob.orbit.logging.LoggingContainerDecorator.OrbitEvents
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.simple.SimpleContext
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.slf4j.Logger

class OrbitSlf4jLogger<STATE : Any, SIDE_EFFECT : Any>(
	private val log: Logger
) : OrbitEvents<STATE, SIDE_EFFECT> {

	override fun intent(
		transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
	) {
		log.trace("Starting intent: {} with {}", transformer.lambdaName(), transformer.captures())
	}

	override fun reduce(
		oldState: STATE,
		reducer: SimpleContext<STATE>.() -> STATE,
		newState: STATE
	) {
		log.trace("reduced via {}:\n{}\n->\n{}", reducer.lambdaName(), oldState, newState)
	}

	override fun sideEffect(
		sideEffect: SIDE_EFFECT
	) {
		log.trace("postSideEffect({})", sideEffect)
	}

	companion object {
		fun <S : Any, SE : Any> Container<S, SE>.decorateLogging(log: Logger): Container<S, SE> =
			LoggingContainerDecorator(this, OrbitSlf4jLogger(log))
	}
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

/**
 * When a lambda is called in Kotlin, it creates an anonymous inner class.
 * This anonymous inner class has fields for the captured variables.
 * When launching the intent, the only parameters captured should be the inputs of the event.
 */
private fun Function<*>.captures(): Map<String, Any?> =
	this::class
		.java
		.declaredFields
		.filter { it.name.startsWith("$") }
		.onEach { it.isAccessible = true }
		.associateBy({ it.name.removePrefix("$") }, { it.get(this) })
