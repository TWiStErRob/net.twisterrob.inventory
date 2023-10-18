package net.twisterrob.inventory.android.database

import java.io.InputStream

fun requireProperty(name: String): String =
	System.getProperty(name) ?: error("Cannot find property: ${name}")

fun Class<*>.requireResourceAsStream(name: String): InputStream =
	this.getResourceAsStream(name) ?: error("Cannot find resource on classpath: ${name}")

fun InputStream.readText(): String =
	this.bufferedReader().use { it.readText() }
