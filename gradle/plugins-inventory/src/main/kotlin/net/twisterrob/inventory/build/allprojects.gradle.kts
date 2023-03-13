package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.libs

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

tasks.withType<JavaCompile>().configureEach javac@{
	this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
		// Enable all warnings the compiler knows.
		"-Xlint:all",
		// Fail build when any warning pops up.
		"-Werror",
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
	if ( // 9 <= Java < 17
		javaVersion.isCompatibleWith(JavaVersion.VERSION_1_9)
		&& !javaVersion.isCompatibleWith(JavaVersion.VERSION_17)
	) {
		jvmArgs(
			"--illegal-access=deny",
		)
	}
}
