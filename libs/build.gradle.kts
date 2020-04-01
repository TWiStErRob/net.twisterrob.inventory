val android = gradle.includedBuild("twister-libs-android")
val java = gradle.includedBuild("twister-libs-java")

/**
 * When running an androidTest from the included build, Android Studio executes the wrong tasks.
 * > Executing tasks: [:capture_image:assembleDebug, :capture_image:assembleDebugAndroidTest] in project I:\
 * > Configure project :android
 * > FAILURE: Build failed with an exception.
 * > * What went wrong:
 * > Project 'capture_image' not found in root project 'Inventory'.
 * 
 * To work around with this change Gradle Aware Make run configuration
 * to execute task `:libs:prepareIncludedAndroidTest`
 * in the Android Instrumented Test run configuration's before launch section.
 * 
 * Make sure to update the `modulePath` in the task definition.
 */
tasks.register("prepareIncludedAndroidTest") {
	val modulePath = ":capture_image"
	dependsOn(android.task("${modulePath}:assembleDebugAndroidTest"))
}
