### -- Inventory/proguard.pro -- ###

# Debugging helpers
#-dontobfuscate
#-dontoptimize
#-optimizationpasses 2
-verbose

# See net.twisterrob.inventory.android.activity.BackupActivity.shouldDisplay
-keepclassmembernames class net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter {
	*** copyXSLT(...);
}

# DialogTools.pickColor is not used in Inventory. It's fine to ignore these.
# Warning: net.twisterrob.android.utils.tools.DialogTools: can't find referenced class com.rarepebble.colorpicker.ColorPickerView
# Warning: net.twisterrob.android.utils.tools.DialogTools$14: can't find referenced class com.rarepebble.colorpicker.ColorPickerView
-dontwarn com.rarepebble.colorpicker.ColorPickerView

# Remove Logging for now
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
