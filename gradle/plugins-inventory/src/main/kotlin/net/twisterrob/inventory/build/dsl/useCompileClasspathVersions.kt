package net.twisterrob.inventory.build.dsl

import com.android.build.api.variant.HasAndroidTest

/**
 * Usage:
 * ```gradle
 * androidComponents {
 *     useCompileClasspathVersions()
 * }
 * ```
 * @see org.gradle.api.plugins.JavaResolutionConsistency.useCompileClasspathVersions
 */
@Suppress("UnstableApiUsage")
fun com.android.build.api.variant.AndroidComponentsExtension<*, *, *>.useCompileClasspathVersions() {
	onVariants { variant ->
		variant.runtimeConfiguration.shouldResolveConsistentlyWith(variant.compileConfiguration)
		variant.unitTest?.let { test ->
			test.runtimeConfiguration.shouldResolveConsistentlyWith(test.compileConfiguration)
		}
		if (variant is HasAndroidTest) {
			variant.androidTest?.let { androidTest ->
				androidTest.runtimeConfiguration.shouldResolveConsistentlyWith(androidTest.compileConfiguration)
			}
		}
		variant.unitTest?.compileConfiguration?.shouldResolveConsistentlyWith(variant.compileConfiguration)
	}
}
