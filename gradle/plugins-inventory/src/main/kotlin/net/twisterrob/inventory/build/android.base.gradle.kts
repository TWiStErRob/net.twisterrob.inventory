package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.android
import net.twisterrob.inventory.build.dsl.autoNamespace

@Suppress("UnstableApiUsage")
android {
	namespace = project.autoNamespace
	compileSdk = 31
	defaultConfig {
		minSdk = 21
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_7
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	lint {
		checkReleaseBuilds = false
		baseline = rootDir.resolve("config/lint/lint-baseline-${project.name}.xml")
		lintConfig = rootDir.resolve("config/lint/lint.xml")
	}
}
