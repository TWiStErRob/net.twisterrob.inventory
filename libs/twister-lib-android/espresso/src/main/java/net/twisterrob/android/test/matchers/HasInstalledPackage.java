package net.twisterrob.android.test.matchers;

import java.util.*;

import org.hamcrest.*;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class HasInstalledPackage extends TypeSafeDiagnosingMatcher<Context> {

	private static final int NORMAL_PACKAGE = 0;

	private final String packageName;

	public HasInstalledPackage(String packageName) {
		this.packageName = packageName;
	}

	@Override public void describeTo(Description description) {
		description.appendValue(packageName).appendText(" package installed");
	}

	@Override protected boolean matchesSafely(Context context, Description mismatchDescription) {
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo(packageName, NORMAL_PACKAGE);
		} catch (NameNotFoundException e) {
			Iterable<PackageInfo> packages = context.getPackageManager().getInstalledPackages(NORMAL_PACKAGE);
			Collection<String> packageNames = new ArrayList<>();
			for (PackageInfo aPackage : packages) {
				packageNames.add(aPackage.packageName);
			}
			mismatchDescription.appendValueList(
					"The following packages were installed: ", ", ", ".", packageNames);
			return false;
		}

		if (!info.applicationInfo.enabled) {
			mismatchDescription.appendValue(packageName).appendText(" is disabled");
			return false;
		}

		return true;
	}
}
