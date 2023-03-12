plugins {
	id("net.twisterrob.gradle.plugin.root")
	id("net.twisterrob.gradle.plugin.quality")
	id("org.gradle.idea")
	id("net.twisterrob.inventory.build.allprojects")
}

// To get gradle/dependency-locks run `gradlew :allDependencies --write-locks`.
project.tasks.register<Task>("allDependencies") {
	val projects = project.allprojects.sortedBy { it.name }
	doFirst {
		println(projects.joinToString(prefix = "Printing dependencies for modules:\n", separator = "\n") { " * ${it}" })
	}
	val dependenciesTasks = projects.map { it.tasks.named("dependencies") }
	// Builds a dependency chain: 1 <- 2 <- 3 <- 4, so when executed they're in order.
	dependenciesTasks.reduce { acc, task -> task.apply { get().dependsOn(acc) } }
	// Use finalizedBy instead of dependsOn to make sure this task executes first.
	this@register.finalizedBy(dependenciesTasks)
}
