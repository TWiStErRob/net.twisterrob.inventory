### -- twister-lib-android/proguard.pro -- ###

# TODO http://stackoverflow.com/q/36817266/253468
#-assumenosideeffects class ** {
#	@net.twisterrob.java.annotations.DebugHelper <methods>;
#}
#-whyareyoukeeping class net.twisterrob.java.annotations.DebugHelper

# net.twisterrob.android.utils.tools.AndroidTools.findActionBarTitle(android.view.View)
# net.twisterrob.android.utils.tools.AndroidTools.findActionBarSubTitle(android.view.View)
-keepclassmembernames class android.support.v7.widget.Toolbar {
	android.widget.TextView mTitleTextView;
	android.widget.TextView mSubtitleTextView;
}
# net.twisterrob.android.utils.tools.AndroidTools.showActionBarOverflowIcons(android.view.Menu,boolean)
-keepclassmembers class **.MenuBuilder {
	void setOptionalIconsVisible(boolean);
}
# net.twisterrob.android.utils.log.LoggingFragment.onActivityCreated(android.os.Bundle)
# net.twisterrob.android.utils.log.LoggingFragment.getLoaderManager()
# net.twisterrob.android.utils.log.LoggingActivity.onCreate(android.os.Bundle)
-keepclassmembernames class android.support.v4.app.LoaderManagerImpl {
	java.lang.String mWho;
}
# net.twisterrob.android.utils.log.LoggingFragment.getName()
-keepclassmembernames class android.support.v4.app.Fragment {
	java.lang.String mWho;
}
# net.twisterrob.android.utils.tools.AndroidTools.toString(java.lang.Object)
-keepclassmembernames class android.support.v4.app.Fragment.SavedState {
	java.lang.String mState;
}
# Note: net.twisterrob.java.utils.CollectionTools accesses a declared field 'map|header|before|key|backingMap|prv' dynamically
#      Maybe this is library field 'class { type field; }'
# Ignore tryGetLastJava/tryGetLastAndroid
-dontnote net.twisterrob.java.utils.CollectionTools

### caverock/androidsvg
# Warning: com.caverock.androidsvg.SVGImageView: can't find referenced class com.caverock.androidsvg.R
# Warning: com.caverock.androidsvg.SVGImageView: can't find referenced class com.caverock.androidsvg.R$styleable
-dontwarn com.caverock.androidsvg.SVGImageView
# Note: the configuration keeps the entry point 'com.caverock.androidsvg.SVGImageView { void setSVG(com.caverock.androidsvg.SVG); }', but not the descriptor class 'com.caverock.androidsvg.SVG'
-dontnote com.caverock.androidsvg.SVGImageView
-dontwarn com.caverock.androidsvg.R**
# com.caverock.androidsvg.CSSParser.selectorMatch
-keepattributes InnerClasses # so that SVG.Line.class.getSimpleName() -> "Line" instead of "SVG$Line"
-keepnames class * extends com.caverock.androidsvg.SVG$SvgElementBase

# Note necessary, but consequence of keeping the InnerClasses attribute
# warning: Ignoring InnerClasses attribute for an anonymous inner class (...) that doesn't come with an associated EnclosingMethod attribute.
# This class was probably produced by a compiler that did not target the modern .class file format.
# The recommended solution is to recompile the class from source, using an up-to-date compiler and without specifying any "-target" type options.
# The consequence of ignoring this warning is that reflective operations on this class will incorrectly indicate that it is *not* an inner class.
-keepattributes EnclosingMethod

### bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule

# TODO is this needed for AboutActivity?
#-keep public class **.BuildConfig
