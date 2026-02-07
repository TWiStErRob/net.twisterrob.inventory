import net.twisterrob.gradle.doNotNagAbout

plugins {
	id("net.twisterrob.gradle.plugin.nagging") version "0.19"
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
	}
	versionCatalogs {
		create("libs") {
			from(files("../../gradle/libs.versions.toml"))
		}
	}
}

val gradleVersion: String = GradleVersion.current().version

// TODEL Gradle 8.13 vs AGP 8.0-8.9 https://issuetracker.google.com/issues/370546370
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring 'crunchPngs' as a property using an 'is-' method with a Boolean type on " +
			"com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated has been deprecated. " +
			"Starting with Gradle 10, this property will no longer be treated like a property. " +
			"The combination of method name and return type is not consistent with Java Bean property rules. " +
			"Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.gradle.internal.dsl.BuildType\$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring 'useProguard' as a property using an 'is-' method with a Boolean type on " +
			"com.android.build.gradle.internal.dsl.BuildType has been deprecated. " +
			"Starting with Gradle 10, this property will no longer be treated like a property. " +
			"The combination of method name and return type is not consistent with Java Bean property rules. " +
			"Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
@Suppress("detekt.MaxLineLength")
doNotNagAbout(
	"Declaring 'wearAppUnbundled' as a property using an 'is-' method with a Boolean type on " +
			"com.android.build.api.variant.impl.ApplicationVariantImpl has been deprecated. " +
			"Starting with Gradle 10, this property will no longer be treated like a property. " +
			"The combination of method name and return type is not consistent with Java Bean property rules. " +
			"Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, " +
			"or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'. " +
			"Consult the upgrading guide for further information: " +
			"https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#groovy_boolean_properties"
)
