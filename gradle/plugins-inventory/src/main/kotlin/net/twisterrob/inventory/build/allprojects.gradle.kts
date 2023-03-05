package net.twisterrob.inventory.build

apply(from = rootDir.resolve("gradle/substitutions.gradle"))

configurations.all {
	if (this.name == "lintClassPath") return@all
//		this.resolutionStrategy.failOnVersionConflict()
}
