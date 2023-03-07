import net.twisterrob.inventory.build.tests.GenerateUtpDependenciesTask

plugins {
	id("net.twisterrob.root")
	id("net.twisterrob.quality")
	id("org.gradle.idea")
	id("net.twisterrob.inventory.build.allprojects")
}

tasks.register<GenerateUtpDependenciesTask>("generateUTPDependencies")
