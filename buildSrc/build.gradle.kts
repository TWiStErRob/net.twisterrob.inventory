@file:Suppress("PropertyName")

import java.util.*

plugins {
	java
	groovy
	`kotlin-dsl`
}

val props = Properties()
	.apply { file("../gradle.properties").inputStream().use { load(it) } }

repositories {
	apply(from = file("../gradle/repos.gradle"))
	maven { name = "TWiStErRob"; url = uri("https://dl.bintray.com/twisterrob/maven") }
}

val VERSION_TWISTER_GRADLE: String by props
val VERSION_TWISTER_QUALITY: String by props
val VERSION_JUNIT: String by props

dependencies {
	//configurations.classpath.resolutionStrategy.cacheChangingModulesFor 0, 'seconds' // -SNAPSHOT
	implementation("net.twisterrob.gradle:plugin:${VERSION_TWISTER_GRADLE}")
	// TODEL this should come from :plugin, but it is not transitive for some reason
	implementation("com.android.tools.build:gradle:3.4.2")
	implementation("com.android.tools.build:builder:3.4.2")
	implementation("net.twisterrob.gradle:twister-quality:${VERSION_TWISTER_QUALITY}")

	testImplementation("junit:junit:${VERSION_JUNIT}")
}

tasks.withType<GroovyCompile> {
	groovyOptions.configurationScript = file("../gradle/groovyc.groovy")
}

kotlinDslPluginOptions {
	experimentalWarning.set(false)
}

gradlePlugin {
	plugins {
		create("database") {
			id = "net.twisterrob.inventory.database"
			implementationClass = "InventoryDatabasePlugin"
		}
		create("mapping") {
			id = "net.twisterrob.inventory.mapping"
			implementationClass = "MappingPlugin"
		}
		create("android-test") {
			id = "net.twisterrob.inventory.androidTest"
			implementationClass = "AndroidTestSetupPlugin"
		}
	}
}
