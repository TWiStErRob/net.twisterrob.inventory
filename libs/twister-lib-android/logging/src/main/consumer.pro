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
