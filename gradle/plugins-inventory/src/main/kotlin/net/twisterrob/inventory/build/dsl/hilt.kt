package net.twisterrob.inventory.build.dsl

import dagger.hilt.android.plugin.HiltExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

/**
 * Created by [dagger.hilt.android.plugin.HiltGradlePlugin].
 */
internal val Project.hilt: HiltExtension
	get() = this.extensions["hilt"] as HiltExtension

internal fun Project.hilt(block: Action<HiltExtension>) {
	block.execute(hilt)
}
