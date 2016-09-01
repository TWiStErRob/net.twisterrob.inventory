package net.twisterrob.android.test.espresso;

import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.support.test.espresso.*;
import android.support.test.espresso.NoMatchingViewException.Builder;
import android.support.test.espresso.core.deps.guava.collect.*;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.util.*;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewDataInteraction;

public class EspressoExtensions {
	/**
	 * Perform action of waiting for a specific view id.
	 * @see <a href="http://stackoverflow.com/a/22563297/253468">Espresso: Thread.sleep( );</a>
	 */
	public static ViewAction waitFor(final Matcher<View> viewMatcher, final long millis) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isRoot();
			}

			@Override public String getDescription() {
				return "wait for " + StringDescription.asString(viewMatcher) + " for " + millis + " milliseconds.";
			}

			@Override public void perform(final UiController uiController, final View view) {
				uiController.loopMainThreadUntilIdle();
				final long startTime = System.currentTimeMillis();
				final long endTime = startTime + millis;

				long step = Math.max(1, Math.min(50, millis / 10)); // 10th of input between 1..50ms 
				do {
					for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
						if (viewMatcher.matches(child)) {
							return;
						}
					}
					uiController.loopMainThreadForAtLeast(step);
					//uiController.loopMainThreadUntilIdle();
				} while (System.currentTimeMillis() < endTime);

				TimeoutException timeout = new TimeoutException();
				NoMatchingViewException noMatchException = new Builder()
						.withRootView(view)
						.withViewMatcher(viewMatcher)
						.build();
				timeout.initCause(noMatchException);
				throw new PerformException.Builder()
						.withActionDescription(this.getDescription())
						.withViewDescription(HumanReadables.describe(view))
						.withCause(timeout)
						.build();
			}
		};
	}

	public static void rotateDevice(ActivityTestRule<?> activity) {
		activity.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getInstrumentation().waitForIdleSync();
	}

	public static Matcher<Intent> chooser(Matcher<Intent> matcher) {
		return allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(is(Intent.EXTRA_INTENT), matcher));
	}
	public static ViewInteraction onActionBarDescendant(Matcher<View> viewMatcher) {
		// need to wait, otherwise android.support.v4.widget.DrawerLayout.SimpleDrawerListener.onDrawerClosed may not finish
//		getInstrumentation().waitForIdleSync();
		return onView(allOf(viewMatcher, inActionBar()));
	}
	public static Matcher<View> inActionBar() {
		return isDescendantOfA(isActionBar());
	}
	private static Matcher<View> isActionBar() {
		return withResourceName(endsWith(":id/action_bar"));
	}
	public static Matcher<View> withResourceName(String resourceName) {
		return withResourceName(is(resourceName));
	}

	public static Matcher<View> withResourceName(final Matcher<String> resourceNameMatcher) {
		return new TypeSafeDiagnosingMatcher<View>(View.class) {
			@Override public void describeTo(Description description) {
				description.appendText("with resource name: ").appendDescriptionOf(resourceNameMatcher);
			}
			// TODO convert to Condition/Steps
			@Override protected boolean matchesSafely(View view, Description mismatchDescription) {
				int id = view.getId();
				if (id == View.NO_ID || id == 0) {
					mismatchDescription.appendText("view with invalid id: ").appendValue(id);
					return false;
				}
				Resources resources = view.getResources();
				if (resources == null) {
					mismatchDescription.appendText("view with no resources");
					return false;
				}
				String name;
				try {
					name = resources.getResourceName(id);
				} catch (Resources.NotFoundException ex) {
					mismatchDescription.appendText("view with id without a name: ").appendValue(ex);
					return false;
				}
				boolean matches = resourceNameMatcher.matches(name);
				if (!matches) {
					resourceNameMatcher.describeMismatch(name, mismatchDescription);
					return false;
				}
				return true;
			}
		};
	}

	/** @see Espresso#onData(Matcher) */
	public static RecyclerViewDataInteraction onRecyclerItem(Matcher<View> dataMatcher) {
		return new RecyclerViewDataInteraction(hasDescendant(dataMatcher));
	}

	/** @see Espresso#onData(Matcher) */
	public static RecyclerViewDataInteraction onRecyclerItemExact(Matcher<View> dataMatcher) {
		return new RecyclerViewDataInteraction(dataMatcher);
	}
	public static Iterable<ViewParent> parentTraversal(final View view) {
		return new Iterable<ViewParent>() {
			@Override public Iterator<ViewParent> iterator() {
				return new AbstractIterator<ViewParent>() {
					private ViewParent current = view.getParent();
					@Override public ViewParent computeNext() {
						if (current == null) {
							return endOfData();
						}
						ViewParent next = current;
						current = current.getParent();
						return next;
					}
				};
			}
		};
	}
	public static Iterable<View> parentViewTraversal(final View view) {
		return new Iterable<View>() {
			@Override public Iterator<View> iterator() {
				return Iterators.filter(parentTraversal(view).iterator(), View.class);
			}
		};
	}
	public static ViewAction scrollToLast() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return anyOf(
						both(isAssignableFrom(AbsListView.class)).and(hasAdapter()),
						both(isAssignableFrom(RecyclerView.class)).and(hasRecyclerAdapter()),
						both(isAssignableFrom(ViewPager.class)).and(hasViewPagerAdapter())
				);
			}
			@Override public String getDescription() {
				return "scroll to last item";
			}
			@Override public void perform(UiController uiController, View view) {
				if (view instanceof AbsListView) {
					AbsListView adapterView = (AbsListView)view;
					adapterView.smoothScrollToPosition(adapterView.getAdapter().getCount() - 1);
				} else if (view instanceof RecyclerView) {
					RecyclerView recycler = (RecyclerView)view;
					recycler.smoothScrollToPosition(recycler.getAdapter().getItemCount() - 1);
				} else if (view instanceof ViewPager) {
					ViewPager pager = (ViewPager)view;
					pager.setCurrentItem(pager.getAdapter().getCount() - 1, true);
				}
			}
		};
	}
	private static Matcher<View> hasViewPagerAdapter() {
		return new BoundedMatcher<View, ViewPager>(ViewPager.class) {
			@Override public void describeTo(Description description) {
				description.appendText("has pager adapter");
			}
			@Override protected boolean matchesSafely(ViewPager item) {
				return item.getAdapter() != null;
			}
		};
	}
	public static Matcher<View> hasRecyclerAdapter() {
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override public void describeTo(Description description) {
				description.appendText("has recycler adapter");
			}
			@Override protected boolean matchesSafely(RecyclerView item) {
				return item.getAdapter() != null;
			}
		};
	}
	private static <A extends Adapter> Matcher<View> hasAdapter() {
		@SuppressWarnings({"unchecked", "rawtypes"}) Class<? extends AdapterView<A>> clazz =
				(Class<? extends AdapterView<A>>)(Class)AdapterView.class;
		return new BoundedMatcher<View, AdapterView<A>>(clazz) {
			@Override public void describeTo(Description description) {
				description.appendText("has adapter");
			}
			@Override protected boolean matchesSafely(AdapterView<A> item) {
				return item.getAdapter() != null;
			}
		};
	}
}
