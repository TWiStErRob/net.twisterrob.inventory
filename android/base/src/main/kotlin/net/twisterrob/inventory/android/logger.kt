package net.twisterrob.inventory.android

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

inline fun <reified T : Any> logger(): Logger =
	logger(T::class)

@Suppress("NOTHING_TO_INLINE")
inline fun logger(klass: KClass<*>): Logger =
	LoggerFactory.getLogger(klass.java)
