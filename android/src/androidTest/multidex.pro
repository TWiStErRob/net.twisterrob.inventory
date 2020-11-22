# Keep all tests in first dex file for OSs with multidex support. (API 21+)
-keepclasseswithmembers class * {
    @org.junit.Test <methods>;
}

# Keep Entry point in Main DEX file
-keep class net.twisterrob.android.test.junit.AndroidJUnitRunner
# Dependencies of Entry point
-keep class androidx.test.runner.**
-keep class net.twisterrob.android.test.junit.internal.DexPathListReflection
-keep class net.twisterrob.java.utils.ReflectionTools
-keep class net.twisterrob.java.utils.StringTools

-keep class org.slf4j.**
-keep class net.twisterrob.android.log.**
