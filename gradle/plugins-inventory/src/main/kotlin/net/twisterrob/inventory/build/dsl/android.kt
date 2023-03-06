package net.twisterrob.inventory.build.dsl

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

/**
 * This is useful to emulate the `android` extension in a convention plugin
 * where the `android-library` or `android-application` are not applied, but implied.
 *
 * @see com.android.build.api.dsl.ApplicationExtension
 * @see com.android.build.api.dsl.LibraryExtension
 */
internal val Project.android: CommonExtension<*, *, *, DefaultConfig, *, *, *, *>
	@Suppress("UNCHECKED_CAST")
	get() = this.extensions["android"] as CommonExtension<*, *, *, DefaultConfig, *, *, *, *>

/**
 * This is useful to emulate the `android` block in a convention plugin
 * where the `android-library` or `android-application` are not applied, but implied.
 *
 * @param block the configuration for common Android things.
 * Using an [Action] to take advantage of `kotlin-dsl`.
 */
internal fun Project.android(block: Action<CommonExtension<*, *, *, DefaultConfig, *, *, *, *>>) {
	block.execute(android)
}
