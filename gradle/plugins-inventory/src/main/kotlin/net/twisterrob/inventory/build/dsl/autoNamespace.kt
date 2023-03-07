package net.twisterrob.inventory.build.dsl

import org.gradle.api.Project

val Project.autoNamespace: String
	get() = "net.twisterrob.inventory.${this.subPackage}"

private val Project.subPackage: String
	get() =
		this.path
			.removePrefix(":")
			.replace(":", ".")
			.replace("-contract", ".contract")
