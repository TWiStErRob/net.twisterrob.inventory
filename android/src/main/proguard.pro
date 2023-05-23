### -- Inventory/proguard.pro -- ###

# Debugging helpers
# -----------------
#-dontobfuscate
#-dontoptimize

# Try to get more output from R8, so far have seen no difference.
# gradlew --info prints
# android.pro:22:1-15: R8: Ignoring option: -optimizations
# twisterrob-release.pro:6:1-22: R8: Ignoring option: -optimizationpasses
# gradlew --debug prints
# [R8] Proguard config
# [R8] Tool config
# [R8] Program classes
# etc, but no details or warnings.
-verbose

# Keep reflectively accessed classes and members
# ----------------------------------------------

# See net.twisterrob.inventory.android.activity.BackupActivity.shouldDisplay
-keepclassmembernames class net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter {
	*** copyXSLT(...);
}

# See net.twisterrob.orbit.logging.LoggingContainerDecoratorKt#captured
-keepclassmembernames class org.orbitmvi.orbit.syntax.simple.SimpleSyntaxExtensionsKt$reduce$* {
    final kotlin.jvm.functions.Function1 $reducer;
}
# See net.twisterrob.orbit.logging.LoggingContainerDecoratorKt#captured
-keepclassmembernames class org.orbitmvi.orbit.syntax.simple.SimpleSyntaxExtensionsKt$intent$* {
    final kotlin.jvm.functions.Function2 $transformer;
}
# See net.twisterrob.orbit.logging.LoggingContainerDecoratorKt#captured
-keepclassmembernames class org.orbitmvi.orbit.syntax.simple.SimpleSyntaxExtensionsKt$blockingIntent$* {
    final kotlin.jvm.functions.Function2 $transformer;
}

# Remove Logging for now
# ----------------------
# FIXME use isLoggable in AndroidLogger and runtime control over TAGs

# slf4j/slf4j-api
-assumenosideeffects interface org.slf4j.Logger {
	public boolean is*Enabled(...);
	public void trace(...);
	public void info(...);
	public void warn(...);
	public void debug(...);
	public void error(...);
}
-assumenosideeffects class org.slf4j.LoggerFactory {
	public static org.slf4j.Logger getLogger(...);
}
# see https://sourceforge.net/p/proguard/bugs/621/
-assumenosideeffects class ** {
	final org.slf4j.Logger LOG;
	static synthetic org.slf4j.Logger access$*();
}

# android logging
-assumenosideeffects class android.util.Log {
	public static boolean isLoggable(java.lang.String, int);
	public static int v(...);
	public static int i(...);
	public static int w(...);
	public static int d(...);
	public static int e(...);
}
