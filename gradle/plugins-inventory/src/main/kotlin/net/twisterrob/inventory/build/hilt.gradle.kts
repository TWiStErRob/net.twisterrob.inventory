package net.twisterrob.inventory.build

//import net.twisterrob.inventory.build.dsl.android
import net.twisterrob.inventory.build.dsl.hilt
import net.twisterrob.inventory.build.dsl.libs
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType

plugins {
	id("org.gradle.java-base")
	id("com.google.dagger.hilt.android")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
}

@Suppress("UnstableApiUsage")
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

hilt {
//	enableExperimentalClasspathAggregation = false // default (2.46.1): false
//	enableAggregatingTask = true // default (2.46.1): true
//	enableTransformForLocalTests = false // default (2.46.1): false
//	disableCrossCompilationRootValidation = false // default (2.46.1): false
}

//val daggerFlags = mapOf(
//	"dagger.experimentalDaggerErrorMessages" to "disabled",
//)
//
//android {
//	defaultConfig {
//		javaCompileOptions {
//			annotationProcessorOptions {
//				daggerFlags.forEach { (key, value) ->
//					@Suppress("UnstableApiUsage")
//					argument(key, value)
//				}
//			}
//		}
//	}
//}
//
//kapt {
//	arguments {
//		daggerFlags.forEach { (key, value) ->
//			arg(key, value)
//		}
//	}
//}
