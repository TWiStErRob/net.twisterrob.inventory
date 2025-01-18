package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.withType<KotlinCompile>().configureEach kotlin@{
	compilerOptions { 
		allWarningsAsErrors.set(true)
		jvmTarget.set(libs.versions.java.android.target.map(JvmTarget::fromTarget))
		freeCompilerArgs.add("-Xcontext-receivers")
	}
}

plugins.withId("org.jetbrains.kotlin.kapt") {
	configure<KaptExtension> {
		correctErrorTypes = true
		mapDiagnosticLocations = true
		strictMode = true
	}
}

tasks.withType<JavaCompile>().configureEach javac@{
	this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
		// Enable all warnings the compiler knows.
		"-Xlint:all",
		// Fail build when any warning pops up.
		"-Werror",
		// TODEL https://issuetracker.google.com/issues/359561906
		// > > Task :android:database-contract:compileDebugJavaWithJavac FAILED
		// > /modules/java.base/java/lang/List.class:
		// > /modules/java.base/java/util/Collections.class:
		// > /modules/java.base/java/lang/Double.class:
		// > warning: Cannot find annotation method 'value()' in type 'FlaggedApi':
		// > class file for android.annotation.FlaggedApi not found
		"-Xlint:-classfile",
	)

	if (this@javac.name.endsWith("UnitTestJavaWithJavac")
		|| this@javac.name.endsWith("AndroidTestJavaWithJavac")
		|| this@javac.path.startsWith(":android:database:test_helpers:")
	) {
		this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
			// Google's compilers emit some weird stuff (espresso, dagger, etc.)
			// warning: [classfile] MethodParameters attribute introduced in version 52.0 class files
			// is ignored in version 51.0 class files
			"-Xlint:-classfile",
		)
	}
}

tasks.withType<Test>().configureEach test@{
	if (javaVersion.isCompatibleWith(JavaVersion.VERSION_1_9)
		&& !javaVersion.isCompatibleWith(JavaVersion.VERSION_17)
	) { // 9 <= Java < 17
		jvmArgs(
			"--illegal-access=deny",
		)
	}
	if (javaVersion.isCompatibleWith(JavaVersion.VERSION_1_9)) { // 9 <= Java
		jvmArgs(
			// net.twisterrob.test.PackageNameShortener.fixPackages uses reflection on Throwable/StackTraceElement.
			"--add-opens", "java.base/java.lang=ALL-UNNAMED",
		)
	}
}
