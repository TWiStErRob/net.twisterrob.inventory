### -- Inventory/proguard.pro -- ###

# Debugging helpers
#-dontobfuscate
#-dontoptimize
#-optimizationpasses 2

# TODEL See res/menu/search.xml and https://issuetracker.google.com/issues/37042902
-keep class androidx.appcompat.widget.SearchView { <init>(...); }


# Rules from versionedparcelable-1.0.0.aar/proguard.txt are triggering this:
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesCompatParcelizer { ... read(VersionedParcel); }', but not the descriptor class 'VersionedParcel'
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesCompatParcelizer { void write(...,VersionedParcel); }', but not the descriptor class 'VersionedParcel'
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplApi21Parcelizer { ... read(VersionedParcel); }', but not the descriptor class 'VersionedParcel'
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplApi21Parcelizer { void write(...,VersionedParcel); }', but not the descriptor class 'VersionedParcel'
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplBaseParcelizer { ... read(VersionedParcel); }', but not the descriptor class 'VersionedParcel'
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplBaseParcelizer { void write(...,VersionedParcel); }', but not the descriptor class 'VersionedParcel'
-dontnote androidx.versionedparcelable.VersionedParcel
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplApi21Parcelizer { void write(androidx.media.AudioAttributesImplApi21,androidx.versionedparcelable.VersionedParcel); }', but not the descriptor class 'androidx.media.AudioAttributesImplApi21'
-dontnote androidx.media.AudioAttributesImplApi21
# Note: the configuration keeps the entry point 'androidx.media.AudioAttributesImplBaseParcelizer { void write(androidx.media.AudioAttributesImplBase,androidx.versionedparcelable.VersionedParcel); }', but not the descriptor class 'androidx.media.AudioAttributesImplBase'
-dontnote androidx.media.AudioAttributesImplBase
# Note: the configuration keeps the entry point 'androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VPath { void setPathData(androidx.core.graphics.PathParser$PathDataNode[]); }', but not the descriptor class 'androidx.core.graphics.PathParser$PathDataNode'
-dontnote androidx.core.graphics.PathParser$PathDataNode


# Rules from core-1.3.2.aar/proguard.txt are triggering this:
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { WindowInsetsCompat$BuilderImpl(androidx.core.view.WindowInsetsCompat); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setSystemWindowInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setSystemGestureInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setMandatorySystemGestureInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setTappableElementInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setStableInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl { void setDisplayCutout(androidx.core.view.DisplayCutoutCompat); }', but not the descriptor class 'androidx.core.view.DisplayCutoutCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl20 { WindowInsetsCompat$BuilderImpl20(androidx.core.view.WindowInsetsCompat); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl20 { void setSystemWindowInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { WindowInsetsCompat$BuilderImpl29(androidx.core.view.WindowInsetsCompat); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setSystemWindowInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setSystemGestureInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setMandatorySystemGestureInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setTappableElementInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setStableInsets(androidx.core.graphics.Insets); }', but not the descriptor class 'androidx.core.graphics.Insets'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$BuilderImpl29 { void setDisplayCutout(androidx.core.view.DisplayCutoutCompat); }', but not the descriptor class 'androidx.core.view.DisplayCutoutCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl { WindowInsetsCompat$Impl(androidx.core.view.WindowInsetsCompat); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl20 { WindowInsetsCompat$Impl20(androidx.core.view.WindowInsetsCompat,android.view.WindowInsets); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl20 { WindowInsetsCompat$Impl20(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl20); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl20 { WindowInsetsCompat$Impl20(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl20); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat$Impl20'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl21 { WindowInsetsCompat$Impl21(androidx.core.view.WindowInsetsCompat,android.view.WindowInsets); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl21 { WindowInsetsCompat$Impl21(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl21); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl21 { WindowInsetsCompat$Impl21(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl21); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat$Impl21'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl28 { WindowInsetsCompat$Impl28(androidx.core.view.WindowInsetsCompat,android.view.WindowInsets); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl28 { WindowInsetsCompat$Impl28(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl28); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl28 { WindowInsetsCompat$Impl28(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl28); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat$Impl28'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl29 { WindowInsetsCompat$Impl29(androidx.core.view.WindowInsetsCompat,android.view.WindowInsets); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl29 { WindowInsetsCompat$Impl29(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl29); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
# Note: the configuration keeps the entry point 'androidx.core.view.WindowInsetsCompat$Impl29 { WindowInsetsCompat$Impl29(androidx.core.view.WindowInsetsCompat,androidx.core.view.WindowInsetsCompat$Impl29); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat$Impl29'
-dontnote androidx.core.view.WindowInsetsCompat$Impl*
-dontnote androidx.core.view.WindowInsetsCompat$BuilderImpl*
# Note: the configuration keeps the entry point 'androidx.core.view.ViewCompat$Api21Impl { androidx.core.view.WindowInsetsCompat computeSystemWindowInsets(android.view.View,androidx.core.view.WindowInsetsCompat,android.graphics.Rect); }', but not the descriptor class 'androidx.core.view.WindowInsetsCompat'
-dontnote androidx.core.view.ViewCompat$Api*Impl


# androidx-multidex:2.0.0 that came with AGP 3.5.4 -> 3.6.4 upgrade doesn't have a consumer proguard file.
# Note: androidx.multidex.MultiDex$V14: can't find dynamically referenced class dalvik.system.DexPathList$Element
-dontnote androidx.multidex.MultiDex$V14


# REPORT name of field is misleading to ProGuard, it's peeking into android.app.AppOpsManager#OP_POST_NOTIFICATION.
# Note: androidx.core.app.NotificationManagerCompat accesses a declared field 'OP_POST_NOTIFICATION' dynamically
#      Maybe this is program field 'androidx.core.app.NotificationManagerCompat { java.lang.String OP_POST_NOTIFICATION; }'
-dontnote androidx.core.app.NotificationManagerCompat


# REPORT GhostView is @hide
# Note: androidx.transition.GhostViewApi21: can't find dynamically referenced class android.view.GhostView
-dontnote androidx.transition.GhostViewApi21
# Note: androidx.transition.GhostViewPlatform: can't find dynamically referenced class android.view.GhostView
-dontnote androidx.transition.GhostViewPlatform


# REPORT SystemProperties is @hide
# Note: androidx.core.view.DisplayCompat: can't find dynamically referenced class android.os.SystemProperties
-dontnote androidx.core.view.DisplayCompat


# REPORT ActivityRecreator is @hide
# Note: androidx.core.app.ActivityRecreator: can't find dynamically referenced class android.app.ActivityThread
-dontnote androidx.core.app.ActivityRecreator


# TODO why?
# Note: androidx.core.graphics.TypefaceCompatApi21Impl: can't find dynamically referenced class android.graphics.FontFamily
# Note: androidx.core.graphics.TypefaceCompatApi24Impl: can't find dynamically referenced class android.graphics.FontFamily
# Note: androidx.core.graphics.TypefaceCompatApi26Impl: can't find dynamically referenced class android.graphics.FontFamily
-dontnote androidx.core.graphics.TypefaceCompatApi21Impl
-dontnote androidx.core.graphics.TypefaceCompatApi24Impl
-dontnote androidx.core.graphics.TypefaceCompatApi26Impl

# REPORT internal? but not hidden, why is it not visible?
# Note: androidx.core.widget.TextViewCompat$OreoCallback: can't find dynamically referenced class com.android.internal.view.menu.MenuBuilder
-dontnote androidx.core.widget.TextViewCompat$OreoCallback
# Note: androidx.core.text.ICUCompat: can't find dynamically referenced class libcore.icu.ICU
-dontnote androidx.core.text.ICUCompat
# Note: androidx.appcompat.app.ResourcesFlusher: can't find dynamically referenced class android.content.res.ThemedResourceCache
-dontnote androidx.appcompat.app.ResourcesFlusher


# It's interrogating val android.app.Notification.extras: Bundle, so it's safe.
# Note: androidx.core.app.NotificationCompatJellybean calls 'Field.getType'
-dontnote androidx.core.app.NotificationCompatJellybean


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
