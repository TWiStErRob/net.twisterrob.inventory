package androidx.fragment.app;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import static org.junit.Assert.*;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import kotlin.collections.CollectionsKt;

import static androidx.fragment.app.testing.FragmentScenario.*;

import net.twisterrob.android.stringers.R;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.android.utils.tosting.strings.AndroidStringerRepoRule;

/**
 * @see SupportBackStackStateStringer
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.P)
@DoNotInstrument
public class SupportBackStackStateStringerTest {

	@Rule public TestRule stringerRule = new AndroidStringerRepoRule();

	@Test
	public void test() {
		final AtomicReference<String> fragment1Who = new AtomicReference<>();
		final AtomicReference<String> fragment2Who = new AtomicReference<>();
		final AtomicReference<String> fragment3Who = new AtomicReference<>();

		FragmentScenario<Fragment1> scenario = launchInContainer(Fragment1.class);
		scenario.onFragment(new FragmentAction<Fragment1>() {
			@Override public void perform(@NonNull Fragment1 fragment1) {
				Fragment2 fragment2 = new Fragment2();
				Fragment3 fragment3 = new Fragment3();
				fragment1Who.set(fragment1.mWho);
				fragment2Who.set(fragment2.mWho);
				fragment3Who.set(fragment3.mWho);
				FragmentManager fragmentManager = fragment1.getParentFragmentManager();
				fragmentManager
						.beginTransaction()
						.addToBackStack("backyStacky")
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.setMaxLifecycle(fragment1, Lifecycle.State.STARTED)
						.setCustomAnimations(
								android.R.anim.fade_in,
								android.R.anim.fade_out,
								android.R.anim.slide_in_left,
								android.R.anim.slide_out_right
						)
						.add(android.R.id.content, fragment2)
						.setCustomAnimations(
								R.anim.fragment_open_enter,
								R.anim.fragment_open_exit,
								R.anim.fragment_fade_enter,
								R.anim.fragment_fade_exit
						)
						.setReorderingAllowed(true)
						.attach(fragment3)
						.commit();
			}
		});

		scenario.onFragment(new FragmentAction<Fragment1>() {
			@Override public void perform(@NonNull Fragment1 fragment) {
				BackStackRecord record =
						CollectionsKt.single(fragment.getParentFragmentManager().mBackStack);
				BackStackState state = new BackStackState(record);

				String result = StringerTools.toString(state);

				assertEquals(
						"(androidx.fragment.app.BackStackState)0: backyStacky, TRANSIT_FRAGMENT_OPEN, reordering allowed, shared::{  }, ops of 3:\n"
								+ "\t" + fragment1Who.get()
								+ ": OP_SET_MAX_LIFECYCLE, MaxLifecycle::{ old=RESUMED, new=STARTED }, Anim::{ enter=invalid, exit=invalid, popEnter=invalid, popExit=invalid }\n"
								+ "\t" + fragment2Who.get()
								+ ": ADD, maxLifecycle=RESUMED, Anim::{ enter=android:anim/fade_in, exit=android:anim/fade_out, popEnter=android:anim/slide_in_left, popExit=android:anim/slide_out_right }\n"
								+ "\t" + fragment3Who.get()
								+ ": ATTACH, maxLifecycle=RESUMED, Anim::{ enter=app:anim/fragment_open_enter, exit=app:anim/fragment_open_exit, popEnter=app:anim/fragment_fade_enter, popExit=app:anim/fragment_fade_exit }",
						result);
			}
		});
	}

	@SuppressWarnings("JUnitTestCaseWithNoTests")
	public static class Fragment1 extends Fragment {
	}

	@SuppressWarnings("JUnitTestCaseWithNoTests")
	public static class Fragment2 extends Fragment {
	}

	@SuppressWarnings("JUnitTestCaseWithNoTests")
	public static class Fragment3 extends Fragment {
	}
}
