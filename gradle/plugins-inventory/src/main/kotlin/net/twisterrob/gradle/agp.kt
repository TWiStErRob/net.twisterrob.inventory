package net.twisterrob.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Project

val Project.androidApp: AppExtension
	get() = this.extensions.findByName("android") as AppExtension
