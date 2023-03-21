import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint

plugins {
	id("org.gradle.java-platform")
}

group = "net.twisterrob.inventory.build"

dependencies {
	javaPlatform.allowDependencies()
	// Lock in the version of Kotlin used so that the transitive dependencies are consistently upgraded.
	// https://kotlinlang.org/docs/whatsnew18.html#usage-of-the-latest-kotlin-stdlib-version-in-transitive-dependencies
	api(platform(libs.kotlin.bom))

	constraints {
		val libsVC = versionCatalogs.named("libs")
		val (androidxTest, androidxProd) = libsVC
			.libraryAliases
			.filter { it.startsWith("androidx.") }
			.partition { it.startsWith("androidx.test.") }

		androidxProd.forEach { apiWithKtx(libsVC.findLibrary(it).get()) }
		androidxTest.forEach { api(libsVC.findLibrary(it).get()) }

		api(libs.test.robolectric)
		api(libs.test.robolectricMultidexShadows)
	}
}

fun DependencyConstraintHandler.apiWithKtx(constraintNotation: Provider<MinimalExternalModuleDependency>) {
	api(constraintNotation) // { version { strictly(version!!) } }
	api(constraintNotation.ktx)
}

val Provider<MinimalExternalModuleDependency>.ktx: Provider<MinimalExternalModuleDependency>
	get() = this.map { it.ktx }

val MinimalExternalModuleDependency.ktx: MinimalExternalModuleDependency
	get() = DefaultMinimalDependency(
		DefaultModuleIdentifier.newId(this.module.group, "${this.module.name}-ktx"),
		DefaultMutableVersionConstraint(this.versionConstraint)
	)
