plugins {
	`kotlin-dsl` // id("org.gradle.kotlin.kotlin-dsl"), but that has a specific version.
	alias(libs.plugins.detekt)
}

group = "net.twisterrob.inventory.build"

gradlePlugin {
	plugins {
		create("database") {
			id = "net.twisterrob.inventory.database"
			implementationClass = "net.twisterrob.inventory.build.database.InventoryDatabasePlugin"
		}
		create("mapping") {
			id = "net.twisterrob.inventory.mapping"
			implementationClass = "net.twisterrob.inventory.build.unfuscation.MappingPlugin"
		}
		create("android-test") {
			id = "net.twisterrob.inventory.androidTest"
			implementationClass = "net.twisterrob.inventory.build.tests.AndroidTestSetupPlugin"
		}
		create("upgrade-test") {
			id = "net.twisterrob.inventory.upgradeTest"
			implementationClass = "net.twisterrob.inventory.build.tests.upgrade.UpgradeTestPlugin"
		}
	}
}

dependencies {
	implementation(libs.plugin.android.gradle)
	implementation(libs.plugin.twisterrob.quality)
	implementation(libs.plugin.twisterrob.android)
	implementation(libs.plugin.kotlin)
	implementation(libs.plugin.dagger.hilt)

	// region: These dependencies were part of AGP in 3.x and 4.x, but in 7.x they became runtime dependencies.
	// UpgradeTestTask: DeviceConnector, DeviceProvider
	compileOnly(libs.plugin.android.tools.testApi)
	// AndroidTestSetupPlugin: IShellEnabledDevice, NullOutputReceiver, UpgradeTestTask: lot
	compileOnly(libs.plugin.android.tools.ddmlib)
	// testRunnerFactory: ProcessExecutor, ExecutorServiceAdapter
	compileOnly(libs.plugin.android.tools.sdkCommon)
	// UpgradeTestTask: FileUtils, StdLogger, ILogger
	compileOnly(libs.plugin.android.tools.common)
	// endregion
	
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
	enableStricterValidation.set(true)
}

detekt {
	allRules = true
	parallel = true
}

tasks.register("cleanFull").configure {
	dependsOn("clean")
}
