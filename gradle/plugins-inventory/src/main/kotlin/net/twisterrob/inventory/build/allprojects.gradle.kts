package net.twisterrob.inventory.build

apply(from = rootDir.resolve("gradle/substitutions.gradle"))

configurations.all {
	if (this.name == "lintClassPath") return@all
//		this.resolutionStrategy.failOnVersionConflict()
}

tasks.withType<JavaCompile>().configureEach javac@{
	this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
		// enable all warnings
		"-Xlint:all",
		// fail build when warnings pop up
		"-Werror",
		// Ignore for now, until understanding what it means.
		// warning: [varargs] Varargs method could cause heap pollution from non-reifiable varargs parameter params
		"-Xlint:-varargs",
		// TODO check my plugin, maybe java-library and java are not compatible there
		// warning: [options] bootstrap class path not set in conjunction with -source 1.7
		"-Xlint:-options",
	)

	if (this@javac.name.endsWith("UnitTestJavaWithJavac")
		|| this@javac.name.endsWith("AndroidTestJavaWithJavac")
		|| this@javac.path.startsWith(":android:database:test_helpers:")) {
		this@javac.options.compilerArgs = this@javac.options.compilerArgs + listOf(
			// Google's compilers emit some weird stuff (espresso, dagger, etc.)
			// warning: [classfile] MethodParameters attribute introduced in version 52.0 class files
			// is ignored in version 51.0 class files
			"-Xlint:-classfile",
		)
	}
}
