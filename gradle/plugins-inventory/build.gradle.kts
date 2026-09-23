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
		create("upgrade-test") {
			id = "net.twisterrob.inventory.upgradeTest"
			implementationClass = "net.twisterrob.inventory.build.tests.upgrade.UpgradeTestPlugin"
		}
	}
}

dependencies {
	implementation(libs.plugins.android.asMarkerArtifact())
	implementation(libs.plugins.twisterrob.quality.asMarkerArtifact())
	implementation(libs.plugins.twisterrob.android.asMarkerArtifact())
	implementation(libs.plugins.kotlin.asMarkerArtifact())
	implementation(libs.plugins.ksp.asMarkerArtifact())
	implementation(libs.plugins.daggerHilt.asMarkerArtifact())

	// region: These dependencies were part of AGP in 3.x and 4.x, but in 7.x they became runtime dependencies.
	// UpgradeTestTask: DeviceConnector, DeviceProvider
	compileOnly(libs.plugin.android.tools.testApi)
	// UpgradeTestTask: lot, TestAwareCustomTestRunListener: CustomTestRunListener
	compileOnly(libs.plugin.android.tools.ddmlib)
	// UpgradeTestTask: FileUtils, StdLogger, ILogger
	compileOnly(libs.plugin.android.tools.common)
	// endregion
	
	// TODEL https://github.com/gradle/gradle/issues/15383
	implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))

	testImplementation(libs.test.junit4)
}

kotlin {
	compilerOptions {
		allWarningsAsErrors = true
	}
}

tasks.named("pluginDescriptors").configure {
	finalizedBy("validatePlugins")
}

tasks.withType<ValidatePlugins>().configureEach {
	ignoreFailures = false
	failOnWarning = true
	enableStricterValidation = true
}

detekt {
	buildUponDefaultConfig = true
	allRules = true
	parallel = true
	config.from("../../config/detekt/detekt.yml")
}

tasks.register("cleanFull").configure {
	dependsOn("clean")
}

fun Provider<PluginDependency>.asMarkerArtifact(): Provider<String> = map { 
	"${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
