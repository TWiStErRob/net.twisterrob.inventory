package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.libs
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType

plugins {
	id("org.gradle.java-base")
	id("com.google.dagger.hilt.android")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
}

dependencies {
	"implementation"(libs.dagger.hilt)
	"kapt"(libs.dagger.hilt.apt)
}

tasks.withType<JavaCompile>().configureEach javac@{
	this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
		// No processor claimed any of these annotations:
		// dagger.hilt.android.HiltAndroidApp
		// android.annotation.*
		// androidx.annotation.*
		// net.twisterrob.*
		"-Xlint:-processing",
	)
}
