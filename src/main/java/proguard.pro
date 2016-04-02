### -- Inventory/proguard.pro -- ###

# Debugging helpers
#-dontobfuscate
#-dontoptimize
#-optimizationpasses 2
-keepattributes EnclosingMethod

# See res/menu/search.xml and b.android.com/170471
-keep class android.support.v7.widget.SearchView { <init>(...); }

# Note: net.twisterrob.inventory.android.content.InventoryProvider calls 'Field.getType'
# Note: there were 1 classes trying to access generic signatures using reflection.
#       You should consider keeping the signature attributes
#       (using '-keepattributes Signature').
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#attributes)
-dontnote net.twisterrob.inventory.android.content.InventoryProvider

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

# android logging
-assumenosideeffects class android.util.Log {
	public static boolean isLoggable(java.lang.String, int);
	public static int v(...);
	public static int i(...);
	public static int w(...);
	public static int d(...);
	public static int e(...);
}

