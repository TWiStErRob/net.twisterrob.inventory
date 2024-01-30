package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.android
import net.twisterrob.inventory.build.dsl.autoNamespace

plugins {
	id("net.twisterrob.inventory.build.hilt")
}

dependencies {
	"implementation"(platform("net.twisterrob.inventory.build:platform-inventory"))
	"testImplementation"(platform("net.twisterrob.inventory.build:platform-inventory"))
	"androidTestImplementation"(platform("net.twisterrob.inventory.build:platform-inventory"))
}

android {
	namespace = project.autoNamespace
	compileSdk = 34
	defaultConfig {
		minSdk = 21
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
		tasks.withType<JavaCompile>().configureEach {
			// warning: [options] source value 7 is obsolete and will be removed in a future release
			options.compilerArgs.add("-Xlint:-options")
		}
	}
	lint {
		checkReleaseBuilds = false
		baseline = rootDir.resolve("config/lint/lint-baseline-${project.name}.xml")
		lintConfig = rootDir.resolve("config/lint/lint.xml")

		/*
		 * Work around
		 * > CannotEnableHidden: Issue Already Disabled
		 * > ../../../android: Issue StopShip was configured with severity fatal in android,
		 * > but was not enabled (or was disabled) in library annotations
		 * > Any issues that are specifically disabled in a library cannot be re-enabled in a dependent project.
		 * > To fix this you need to also enable the issue in the library project.
		 * Strangely even though both projects have my plugin applied which adds the fatal.
		 */
		fatal.remove("StopShip")
	}
}
