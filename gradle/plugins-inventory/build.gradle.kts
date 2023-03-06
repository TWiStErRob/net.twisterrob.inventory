plugins {
	`kotlin-dsl` // id("org.gradle.kotlin.kotlin-dsl"), but that has a specific version.
	alias(libs.plugins.detekt)
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

dependencies {
	implementation(libs.android.gradle)
	implementation(libs.twisterrob.quality)
	implementation(libs.twisterrob.android)
	// TODEL https://github.com/gradle/gradle/issues/15383
	implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
	testImplementation(libs.test.junit4)
}

configurations.all {
	resolutionStrategy {
		dependencySubstitution {
			substitute(module("net.sf.proguard:proguard-gradle"))
				.using(module("com.guardsquare:proguard-gradle:7.3.1"))
				.because("Latest ProGuard is 7.3.1 which supports Java 11-19, Kotlin 1.8")
		}
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		allWarningsAsErrors = true
	}
}

tasks.named("pluginDescriptors").configure {
	finalizedBy("validatePlugins")
}

tasks.withType<ValidatePlugins>().configureEach {
	ignoreFailures.set(false)
	failOnWarning.set(true)
	enableStricterValidation.set(false)
}

detekt {
	allRules = true
	parallel = true
}
