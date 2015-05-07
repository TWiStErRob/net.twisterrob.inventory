### -- twister-lib-android/proguard.pro -- ###

-assumenosideeffects class ** {
	@net.twisterrob.java.annotations.DebugHelper <methods>;
}
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

### bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule
