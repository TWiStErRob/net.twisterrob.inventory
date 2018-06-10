# net.twisterrob.android.utils.tools.AndroidTools.toString(java.lang.Object)
-keepclassmembernames class android.support.v4.app.Fragment.SavedState {
	java.lang.String mState;
}

# debug helpers (may be needed, wrapped most their usages in BuildConfig.DEBUG for now)
#-assumenosideeffects class net.twisterrob.android.utils.tools.AndroidTools {
#	public static java.lang.String toString(...);
#	public static java.lang.String toShortString(...);
#	public static java.lang.String toNameString(...);
#}
# Try to remove BaseApp call to AndroidStringerRepo.init() to enable removal of all the stringers and annotations
-assumenosideeffects class net.twisterrob.android.utils.tostring.stringers.AndroidStringerRepo {
	*** init(...);
}
-assumenosideeffects class net.twisterrob.java.utils.tostring.StringerRepo {
	*** getInstance();
}
