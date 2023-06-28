App to generate all SVG files.

## Firebase
This is an app instead of a test project, because Firebase Testlab requires and app APK and a test APK.

## Local & Manual CI
```
gradlew :android:data:svg:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=net.twisterrob.inventory.android.data.svg.DumpImages -Pandroid.experimental.androidTest.useUnifiedTestPlatform=true
```
will output on device to:
```
/storage/sdcard/Android/data/net.twisterrob.inventory.data.debug/files/test_data/svg_<sdk>.zip
```
which is then pulled by AGP's `connectedCheck` as
```
android/data/svg/build/outputs/connected_android_test_additional_output/debugAndroidTest/connected/<emulator name>/svg_<sdk>.zip
```

## Gradle Managed Devices
Only work above 29
```
gradlew
    :android:data:svg:gmdGroupDebugAndroidTest
    -Pandroid.testInstrumentationRunnerArguments.class=net.twisterrob.inventory.android.data.svg.DumpImages
    -Pandroid.experimental.androidTest.useUnifiedTestPlatform=true
    --continue
```
output is in:
```
android/data/svg/build/outputs/managed_device_android_test_additional_output/<gmd name>/svg_<sdk>.zip
```

Potential diagnostic helpers:
```
    -Pandroid.experimental.testOptions.managedDevices.setupTimeoutMinutes=300
    --max-workers 1
    -Pandroid.experimental.testOptions.managedDevices.maxConcurrentDevices=1
    -Dorg.gradle.workers.max=1
```
