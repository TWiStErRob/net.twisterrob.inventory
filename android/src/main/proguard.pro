### -- Inventory/proguard.pro -- ###

# Debugging helpers
#-dontobfuscate
#-dontoptimize
#-optimizationpasses 2

# See res/menu/search.xml and b.android.com/170471
-keep class android.support.v7.widget.SearchView { <init>(...); }


# Note: net.twisterrob.inventory.android.content.InventoryProvider calls 'Field.getType'
# Note: there were 1 classes trying to access generic signatures using reflection.
#       You should consider keeping the signature attributes
#       (using '-keepattributes Signature').
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#attributes)
-dontnote net.twisterrob.inventory.android.content.InventoryProvider

# See net.twisterrob.inventory.android.activity.BackupActivity.shouldDisplay
-keepclassmembernames class net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter {
	*** copyXSLT(...);
}


# Accessing the Map/Set internals with reflection, no need to guess which one, it's all in libraryjars.
# Note: net.twisterrob.java.utils.CollectionTools accesses a declared field '*' dynamically
-dontnote net.twisterrob.java.utils.CollectionTools


# Disable unique method inlining optimization for 
# android.support.v7.content.res.AppCompatColorStateListInflater
# because it crashes on startup with below on API 10:
# W/dalvikvm: VFY: invalid aput-object on Ljava/lang/Object;
# W/dalvikvm: VFY:  rejecting opcode 0x4d at 0x00f7
# W/dalvikvm: VFY:  rejected Landroid/support/v7/d/a/a;.a (Landroid/content/res/Resources;Lorg/xmlpull/v1/XmlPullParser;ndroid/util/AttributeSet;Landroid/content/res/Resources$Theme;)Landroid/content/res/ColorStateList;
# W/dalvikvm: Verifier rejected class Landroid/support/v7/d/a/a;
# java.lang.VerifyError: android.support.v7.d.a.a
#     at android.support.v7.d.a.b.b(SourceFile:95)
#     at android.support.v7.d.a.b.a(SourceFile:71)
#     at android.support.v7.widget.av.d(SourceFile:132)
#     at android.support.v7.widget.n.a(SourceFile:96)
#     at android.support.v7.widget.AppCompatTextView.<init>(SourceFile:67)
#     at android.support.v7.widget.AppCompatTextView.<init>(SourceFile:56)
#     at android.support.v7.a.n.a(SourceFile:18103)
#     at android.support.v4.view.i$a.onCreateView(SourceFile:36)
#     at android.view.LayoutInflater$FactoryMerger.onCreateView(LayoutInflater.java:135)
#        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:563)
-optimizations !method/inlining/unique


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
