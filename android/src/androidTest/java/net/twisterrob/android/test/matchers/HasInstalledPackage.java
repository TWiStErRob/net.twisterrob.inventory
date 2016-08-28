package net.twisterrob.android.test.matchers;

import org.hamcrest.*;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class HasInstalledPackage extends TypeSafeMatcher<Context> {
	private final String packageName;
	public HasInstalledPackage(String packageName) {
		this.packageName = packageName;
	}
	@Override protected boolean matchesSafely(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			return info != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	@Override public void describeTo(Description description) {
		description.appendText("There is an installed package named: " + packageName);
	}
}
