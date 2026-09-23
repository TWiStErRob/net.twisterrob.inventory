package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

configurations.configureEach {
	resolutionStrategy {
		dependencySubstitution {
			substitute(module(libs.deprecated.hamcrestCore.get().module.toString()))
				.using(module(libs.test.hamcrest.get().toString()))
			substitute(module(libs.deprecated.hamcrestLibrary.get().module.toString()))
				.using(module(libs.test.hamcrest.get().toString()))
		}
	}
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
	configure<KotlinBaseExtension>(KotlinBaseExtension::configureKotlin)
}
pluginManager.withPlugin("org.jetbrains.kotlin.android") {
	configure<KotlinBaseExtension>(KotlinBaseExtension::configureKotlin)
}

fun KotlinBaseExtension.configureKotlin() {
	jvmToolchain {
		languageVersion = libs.versions.java.target.map(JavaLanguageVersion::of)
	}
	this as HasConfigurableKotlinCompilerOptions<*>
	compilerOptions {
		allWarningsAsErrors = true
		freeCompilerArgs.add("-Xcontext-parameters")
		freeCompilerArgs.add("-Xannotation-default-target=param-property")
	}
}

plugins.withId("org.jetbrains.kotlin.kapt") {
	configure<KaptExtension> {
		correctErrorTypes = true
		mapDiagnosticLocations = true
		strictMode = true
	}
}

tasks.withType<JavaCompile>().configureEach {
	this.options.compilerArgs = this.options.compilerArgs + listOf(
		// Enable all warnings the compiler knows.
		"-Xlint:all",
		// Fail build when any warning pops up.
		"-Werror",
	)

	if (this.name.endsWith("UnitTestJavaWithJavac")
		|| this.name.endsWith("AndroidTestJavaWithJavac")
		|| this.path.startsWith(":android:database:test_helpers:")
	) {
		this.options.compilerArgs = this.options.compilerArgs + listOf(
			// Google's compilers emit some weird stuff (espresso, dagger, etc.)
			// warning: [classfile] MethodParameters attribute introduced in version 52.0 class files
			// is ignored in version 51.0 class files
			"-Xlint:-classfile",
		)
	}
}

tasks.withType<Test>().configureEach {
	jvmArgs( // 9 <= Java
		// net.twisterrob.test.PackageNameShortener.fixPackages uses reflection on Throwable/StackTraceElement.
		"--add-opens", "java.base/java.lang=ALL-UNNAMED",
	)
}
