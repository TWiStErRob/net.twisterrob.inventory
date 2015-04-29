### -- Inventory/proguard.pro -- ###

# Debugging helpers
#-dontobfuscate
#-dontoptimize
#-optimizationpasses 2

# See res/menu/search.xml and b.android.com/170471
-keep class android.support.v7.widget.SearchView { <init>(...); }

# Remove Logging for now
# TODO use isLoggable in AndroidLogger and runtime control over TAGs

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
