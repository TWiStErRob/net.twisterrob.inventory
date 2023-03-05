package net.twisterrob.inventory.build

import net.twisterrob.inventory.build.dsl.android

@Suppress("UnstableApiUsage")
android {
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_7
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	lintOptions {
		isCheckReleaseBuilds = false
		baselineFile = rootDir.resolve("config/lint/lint-baseline-${project.name}.xml")
		lintConfig = rootDir.resolve("config/lint/lint.xml")
	}
}
