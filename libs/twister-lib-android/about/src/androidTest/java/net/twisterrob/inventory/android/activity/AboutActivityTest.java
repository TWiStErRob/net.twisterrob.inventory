package net.twisterrob.inventory.android.activity;

import org.junit.*;
//import org.junit.experimental.categories.Category;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.about.R;
import net.twisterrob.android.activity.AboutActivity;
import net.twisterrob.android.test.junit.SensibleActivityTestRule;
import net.twisterrob.inventory.android.test.actors.AboutActivityActor;
//import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

//@Category(On.Support.class)
public class AboutActivityTest {
	@Rule public final ActivityTestRule<AboutActivity> activity = new SensibleActivityTestRule<>(AboutActivity.class);
	private final AboutActivityActor about = new AboutActivityActor();

//	@Category({Op.Rotates.class})
	@Test public void testRotate() {
		about.rotate();
		about.rotate();
	}

//	@Category(UseCase.InitialCondition.class)
	@Test public void testAppPackageShown() {
		about.assertTextExists(containsString(getTargetContext().getPackageName()));
	}

//	@Category(UseCase.InitialCondition.class)
	@Test public void testAppNameShown() {
		int appName = net.twisterrob.android.about.test.R.string.about_test_application_label;
		about.assertTextExists(containsStringRes(appName));
	}

//	@Category(UseCase.InitialCondition.class)
	@Test public void testAppVersionShown() {
		about.assertTextExists(containsString(getPackageInfo().versionName));
		about.assertTextExists(containsString(String.valueOf(getPackageInfo().versionCode)));
	}

//	@Category(UseCase.InitialCondition.class)
	@Test public void testLicencesMentioned() {
		String[] licences = getTargetContext().getResources().getStringArray(R.array.about_licenses);
		for (String licence : licences) {
			about.assertTextExists(equalTo(licence));
		}
	}

//	@Category(UseCase.InitialCondition.class)
	@Test public void testSectionsShown() {
		about.assertTextExists(equalTo(stringRes(R.string.about_faq_title)));
		about.assertTextExists(equalTo(stringRes(R.string.about_help_title)));
		about.assertTextExists(equalTo(stringRes(R.string.about_tips_title)));
	}

	private static PackageInfo getPackageInfo() {
		Context context = getTargetContext();
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			assertThat(context.getPackageName(), info, not(nullValue()));
			return info;
		} catch (NameNotFoundException e) {
			AssertionError fail = new AssertionError("Cannot get package info for " + context.getPackageName());
			//noinspection UnnecessaryInitCause API 19 only
			fail.initCause(e);
			throw fail;
		}
	}
}
