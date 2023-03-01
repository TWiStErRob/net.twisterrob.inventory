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
	implementation("com.android.tools.build:gradle:7.4.2")
	
	// region: These dependencies were part of AGP in 3.x and 4.x, but in 7.x they became runtime dependencies.
	// Execution failed for task ':compileGroovy'.
	// > com.android.ddmlib.testrunner.XmlTestRunListener
	compileOnly("com.android.tools.build:builder-test-api:7.4.2")
	// AndroidTestSetupPlugin
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

	testImplementation("junit:junit:${VERSION_JUNIT}")
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

tasks.withType<GroovyCompile> {
	groovyOptions.configurationScript = file("../gradle/groovyc.groovy")
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
