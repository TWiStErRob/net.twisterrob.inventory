plugins {
	`kotlin-dsl` // id("org.gradle.kotlin.kotlin-dsl"), but that has a specific version.
	alias(libs.plugins.detekt)
}

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
	implementation(libs.android.gradle)
	implementation(libs.twisterrob.quality)
	implementation(libs.twisterrob.android)

	// region: These dependencies were part of AGP in 3.x and 4.x, but in 7.x they became runtime dependencies.
	// Execution failed for task ':compileGroovy'.
	// > com.android.ddmlib.testrunner.XmlTestRunListener
	compileOnly("com.android.tools.build:builder-test-api:7.4.2")
	// AndroidTestSetupPlugin: IShellEnabledDevice, NullOutputReceiver
	compileOnly("com.android.tools.ddms:ddmlib:30.4.2")
	// testRunnerFactory
	compileOnly("com.android.tools:sdk-common:30.4.2")
	// UpgradeTestTask: StdLogger, ILogger
	compileOnly("com.android.tools:common:30.4.2")
	// > Task :compileGroovy
	// General error during canonicalization: java.lang.NoClassDefFoundError: com.android.repository.Revision
	compileOnly("com.android.tools:repository:30.4.2")
	// Execution failed for task ':compileGroovy'.
	// > org.kxml2.io.KXmlSerializer
	compileOnly("net.sf.kxml:kxml2:2.3.0")
	// GenerateDebugMappingPlugin
	// MappingPlugin
	compileOnly("net.sf.proguard:proguard-gradle:6.0.3")
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
