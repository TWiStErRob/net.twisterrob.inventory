import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint

plugins {
	id("org.gradle.java-platform")
}

group = "net.twisterrob.inventory.build"

dependencies {
	constraints {
		apiWithKtx(libs.androidx.activity)
		apiWithKtx(libs.androidx.annotation)
		apiWithKtx(libs.androidx.annotationExperimental)
		apiWithKtx(libs.androidx.appcompat)
		apiWithKtx(libs.androidx.appcompatResources)
		apiWithKtx(libs.androidx.archCoreCommon)
		apiWithKtx(libs.androidx.archCoreRuntime)
		apiWithKtx(libs.androidx.cardview)
		apiWithKtx(libs.androidx.collection)
		apiWithKtx(libs.androidx.concurrentFutures)
		apiWithKtx(libs.androidx.constraintlayout)
		apiWithKtx(libs.androidx.coordinatorlayout)
		apiWithKtx(libs.androidx.core)
		apiWithKtx(libs.androidx.cursoradapter)
		apiWithKtx(libs.androidx.customview)
		apiWithKtx(libs.androidx.documentfile)
		apiWithKtx(libs.androidx.drawerlayout)
		apiWithKtx(libs.androidx.dynamicanimation)
		apiWithKtx(libs.androidx.exifinterface)
		apiWithKtx(libs.androidx.fragment)
		apiWithKtx(libs.androidx.interpolator)
		apiWithKtx(libs.androidx.lifecycleCommon)
		apiWithKtx(libs.androidx.lifecycleRuntime)
		apiWithKtx(libs.androidx.lifecycleViewModel)
		apiWithKtx(libs.androidx.loader)
		apiWithKtx(libs.androidx.localbroadcastmanager)
		apiWithKtx(libs.androidx.material)
		apiWithKtx(libs.androidx.multidex)
		apiWithKtx(libs.androidx.preference)
		apiWithKtx(libs.androidx.print)
		apiWithKtx(libs.androidx.recyclerview)
		apiWithKtx(libs.androidx.savedstate)
		apiWithKtx(libs.androidx.slidingpanelayout)
		apiWithKtx(libs.androidx.swiperefreshlayout)
		apiWithKtx(libs.androidx.transition)
		apiWithKtx(libs.androidx.vectordrawable)
		apiWithKtx(libs.androidx.viewpager)
		apiWithKtx(libs.androidx.viewpager2)

		api(libs.androidx.test.junit)
		api(libs.androidx.test.core)
		api(libs.androidx.test.runner)
		api(libs.androidx.test.rules)
		api(libs.androidx.test.monitor)
		api(libs.androidx.test.espressoCore)
		api(libs.androidx.test.espressoIntents)
		api(libs.androidx.test.espressoContrib)
		api(libs.androidx.test.espressoIdlingResource)
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
