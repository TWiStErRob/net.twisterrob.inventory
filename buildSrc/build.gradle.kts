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
	//maven { name = "sonatype1"; setUrl("https://s01.oss.sonatype.org/service/local/repositories/nettwisterrob-1013/content/") }
	apply(from = file("../gradle/repos.gradle"))
}

val VERSION_TWISTER_GRADLE: String by props
val VERSION_TWISTER_QUALITY: String by props
val VERSION_JUNIT: String by props

dependencies {
	configurations["implementation"].resolutionStrategy.cacheChangingModulesFor(0, "seconds") // -SNAPSHOT
	implementation("net.twisterrob.gradle:twister-convention-plugins:${VERSION_TWISTER_GRADLE}")
	implementation("net.twisterrob.gradle:twister-quality:${VERSION_TWISTER_QUALITY}")
	implementation("com.android.tools.build:gradle:4.1.3")

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions.allWarningsAsErrors = false
}

tasks.withType<JavaCompile> {
	options.compilerArgs = options.compilerArgs + listOf(
		"-Xlint:all",
		"-Werror"
	)
}
