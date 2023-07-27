package net.twisterrob.inventory.build

//import net.twisterrob.inventory.build.dsl.android
import com.android.build.api.variant.ComponentIdentity
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.HasUnitTest
import net.twisterrob.gradle.android.androidComponents
import net.twisterrob.inventory.build.dsl.hilt
import net.twisterrob.inventory.build.dsl.libs

plugins {
	id("org.gradle.java-base")
	id("com.google.dagger.hilt.android")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
	id("org.gradle.idea")
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

androidComponents.onVariants { variant ->
	// FIXME Doesn't tie the generated sources to the variant ([main] generated sources root)
	// So the files are full of errors and indexing doesn't link them up either.
	// Anyway, at least they're navigable by hand and full text search.
	val addGeneratedSources: (File) -> Unit = { dir: File ->
		// Have to add it to both so that it's marked correctly at
		// org.gradle.plugins.ide.internal.tooling.IdeaModelBuilder.srcDirs.
		idea.module.sourceDirs.add(dir)
		idea.module.generatedSourceDirs.add(dir)
	}
	variant.componentTreesDir.let(addGeneratedSources)
	variant.componentSourcesDir.let(addGeneratedSources)
	(variant as? HasUnitTest)?.unitTest?.componentTreesDir?.let(addGeneratedSources)
	(variant as? HasUnitTest)?.unitTest?.componentSourcesDir?.let(addGeneratedSources)
	(variant as? HasAndroidTest)?.androidTest?.componentTreesDir?.let(addGeneratedSources)
	(variant as? HasAndroidTest)?.androidTest?.componentSourcesDir?.let(addGeneratedSources)
}

val ComponentIdentity.componentTreesDir: File
	get() = hiltGenerated("component_tree").dir(name).get().asFile

val ComponentIdentity.componentSourcesDir: File
	get() = hiltGenerated("component_sources").dir(name).get().asFile

/**
 * @see dagger.hilt.android.plugin.HiltGradlePlugin
 */
fun Project.hiltGenerated(path: String): Provider<Directory> =
	layout.buildDirectory.dir("generated/hilt").dir(path)

fun Provider<Directory>.dir(name: String): Provider<Directory> =
	map { it.dir(name) }
