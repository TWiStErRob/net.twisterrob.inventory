package net.twisterrob.android.test.espresso;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.*;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.*;
import android.support.annotation.*;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.Beta;
import android.support.test.espresso.*;
import android.support.test.espresso.NoMatchingViewException.Builder;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.core.internal.deps.guava.base.Predicate;
import android.support.test.espresso.core.internal.deps.guava.collect.*;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.util.*;
import android.support.test.runner.lifecycle.Stage;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewDataInteraction;
import net.twisterrob.android.test.junit.InstrumentationExtensions;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.test.junit.FlakyTestException;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.java.utils.ObjectTools.*;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class EspressoExtensions {
	private static final Logger LOG = LoggerFactory.getLogger(EspressoExtensions.class);
	/** @see android.support.test.espresso.action.MotionEvents#sendDown */
	private static final String OVERSLEEP_MESSAGE = "Overslept and turned a tap into a long press";
	/** @see android.support.test.espresso.action.MotionEvents#TAG */
	private static final String OVERSLEEP_TAG = "MotionEvents";

	public static ViewAssertion exists() {
		return matches(anything());
	}
	/** @see <a href="http://stackoverflow.com/a/39436238/253468">Espresso: return boolean if view exists</a> */
	public static boolean exists(ViewInteraction interaction) {
		try {
			interaction.perform(new ViewExists());
			return true;
		} catch (AmbiguousViewMatcherException ex) {
			// if there's any interaction later with the same matcher, that'll fail anyway
			return true; // we found more than one
		} catch (NoMatchingRootException | NoMatchingViewException ex) {
			return false;
		}
	}
	public static boolean exists(DataInteraction interaction) {
		try {
			interaction.perform(new ViewExists());
			return true;
		} catch (AmbiguousViewMatcherException ex) {
			// if there's any interaction later with the same matcher, that'll fail anyway
			return true; // we found more than one
		} catch (PerformException | NoMatchingRootException | NoMatchingViewException ex) {
			return false;
		}
	}
	private static class ViewExists implements ViewAction {
		@Override public Matcher<View> getConstraints() {
			return any(View.class);
		}
		@Override public String getDescription() {
			return "check for existence";
		}
		@Override public void perform(UiController uiController, View view) {
			// no op, if this is run, then the execution will continue after .perform(...)
		}
	}

	/**
	 * Version of {@link ViewAssertions#doesNotExist()} that tries to match the matcher if the view would exist.
	 * To get the behavior of the original assertion use {@code .check(doesNotExists(not(anything())))}.
	 */
	public static ViewAssertion doesNotExist(final Matcher<View> fallbackMatcher) {
		return new ViewAssertion() {
			@Override public void check(View view, NoMatchingViewException noView) {
				if (view != null) {
					String description = "View is present in the hierarchy: " + HumanReadables.describe(view);
					assertThat(description, view, fallbackMatcher);
				}
			}
		};
	}

	public static ViewInteraction onRoot() {
		return onView(isRoot());
	}
	public static ViewInteraction onRoot(Matcher<Root> rootMatcher) {
		return onView(isRoot()).noActivity().inRoot(rootMatcher);
	}

	public static ViewAction loopMainThreadUntilIdle() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return any(View.class);
			}
			@Override public String getDescription() {
				return "loop main thread until idle";
			}
			@Override public void perform(UiController uiController, View view) {
				uiController.loopMainThreadUntilIdle();
			}
		};
	}
	public static ViewAction loopMainThreadForAtLeast(final long time) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return any(View.class);
			}
			@Override public String getDescription() {
				return "loop main thread for at least " + time + " milliseconds";
			}
			@Override public void perform(UiController uiController, View view) {
				uiController.loopMainThreadForAtLeast(time);
			}
		};
	}
	public static UiController getUIControllerHack() {
		try {
			ViewInteraction interaction = onView(any(View.class));
			Field field = ViewInteraction.class.getDeclaredField("uiController");
			field.setAccessible(true);
			return (UiController)field.get(interaction);
		} catch (Exception ex) {
			throw new IllegalStateException("Cannot find a UIController");
		}
	}

	@DebugHelper
	public static UiController getUIController() {
		final AtomicReference<UiController> controller = new AtomicReference<>();
		onView(isRoot()).perform(new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return any(View.class);
			}
			@Override public String getDescription() {
				return "steal UI controller";
			}
			@Override public void perform(UiController uiController, View view) {
				controller.set(uiController);
			}
		});
		return controller.get();
	}

	public static @Nullable View stealView(@NonNull Matcher<View> viewMatcher) {
		class StealViewAction implements ViewAction {
			private View matchedView;
			@Override public Matcher<View> getConstraints() {
				return anyView();
			}
			@Override public String getDescription() {
				return "steal view";
			}
			@Override public void perform(UiController uiController, View view) {
				matchedView = view;
			}
		}
		StealViewAction stealView = new StealViewAction();
		onView(viewMatcher).withFailureHandler(new Ignore()).perform(stealView);
		return stealView.matchedView;
	}

	/**
	 * Lazy version of an idling resource that waits for a view to become available.
	 * <code>onDelayedView(isDialogView(), 100).perform(clickNeutral());</code>
	 * TODO the root is locked on the first call, try to get a fresh one every so root(rootMatcher) can work too
	 */
	@Beta
	public static ViewInteraction onDelayedView(final Matcher<View> viewMatcher, final long timeout) {
		final AtomicReference<ViewInteraction> result = new AtomicReference<>();
		onView(isRoot()).perform(new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isRoot();
			}

			@Override public String getDescription() {
				return "wait for " + viewMatcher + " for " + timeout + " milliseconds.";
			}

			@Override public void perform(final UiController uiController, final View view) {
				final long startTime = System.currentTimeMillis();
				final long endTime = startTime + timeout;

				long step = Math.max(1, Math.min(50, timeout / 10)); // 10th of input between 1..50ms 
				timeout:
				do {
					LOG.trace("Checking for {}", viewMatcher);
					for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
						if (viewMatcher.matches(child)) {
							ViewInteraction interaction = onView(viewMatcher);
							result.set(interaction);
							LOG.info("Found it: {}", interaction);
							break timeout;
						}
					}
					LOG.trace("Stepping: {}", step);
					uiController.loopMainThreadForAtLeast(step);
				} while (System.currentTimeMillis() < endTime);

				if (result.get() == null) {
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
			}
		});
		return result.get();
	}

	private static @NonNull BaseLayerComponent getEspressoBASE() {
		return checkNotNull((BaseLayerComponent)ReflectionTools.getStatic(Espresso.class, "BASE"));
	}

	public static List<Root> getRoots() {
		try {
			return InstrumentationExtensions.callOnMainIfNecessary(new Callable<List<Root>>() {
				@Override public List<Root> call() {
					return getEspressoBASE().activeRootLister().listActiveRoots();
				}
			});
		} catch (Exception ex) {
			LOG.error("Cannot retrieve roots", ex);
			return Collections.emptyList();
		}
	}

	public static boolean hasRoot(Matcher<Root> matcher) {
		for (Root root : getRoots()) {
			if (matcher.matches(root)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Perform action of waiting for a specific view id.
	 * @see <a href="http://stackoverflow.com/a/22563297/253468">Espresso: Thread.sleep( );</a>
	 */
	@SuppressLint("LogConditional")
	public static ViewInteraction waitForRoot(final Matcher<Root> rootMatcher, final long timeout) {
		String actionDescription = String.format(Locale.ROOT, "wait for at most %d milliseconds to appear", timeout);
		Log.i("ViewInteraction", String.format(Locale.ROOT, "Performing '%s' action on root %s",
				actionDescription, StringDescription.asString(rootMatcher)));
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + timeout;
		final long step = Math.max(1, Math.min(50, timeout / 10)); // 10th of input between 1..50ms
		do {
			ViewInteraction interaction = onRoot(rootMatcher);
			if (exists(interaction)) {
				return interaction;
			}
			onView(isRoot()).perform(loopMainThreadForAtLeast(step));
		} while (System.currentTimeMillis() < endTime);

		TimeoutException timeoutException = new TimeoutException("Timed out waiting " + timeout + " milliseconds");
		timeoutException.initCause(NoMatchingRootException.create(rootMatcher, getRoots()));
		throw new PerformException.Builder()
				.withActionDescription(actionDescription)
				.withViewDescription(StringDescription.asString(rootMatcher))
				.withCause(timeoutException)
				.build();
	}

	@Beta
	public static ViewAction waitFor(final Matcher<View> viewMatcher) {
		IdlingPolicy policy = IdlingPolicies.getDynamicIdlingResourceErrorPolicy();
		long timeout = policy.getIdleTimeoutUnit().toMillis(policy.getIdleTimeout());
		return waitFor(viewMatcher, timeout);
	}
	/**
	 * Perform action of waiting for a specific view id.
	 * @see <a href="http://stackoverflow.com/a/22563297/253468">Espresso: Thread.sleep( );</a>
	 */
	@Beta
	public static ViewAction waitFor(final Matcher<View> viewMatcher, final long timeout) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isRoot();
			}

			@Override public String getDescription() {
				return "wait for " + StringDescription.asString(viewMatcher) + " for " + timeout + " milliseconds.";
			}

			@Override public void perform(final UiController uiController, final View view) {
				LOG.trace("waiting for {} to match {}", HumanReadables.describe(view), viewMatcher);
				final long startTime = System.currentTimeMillis();
				final long endTime = startTime + timeout;
				final long step = Math.max(1, Math.min(50, timeout / 10)); // 10th of input between 1..50ms
				LOG.trace("From {} to {} by {}", startTime, endTime, step);
				do {
					LOG.trace("Checking all children of {}", HumanReadables.describe(view));
					for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
						if (viewMatcher.matches(child)) {
							LOG.trace("Found child: {}", HumanReadables.describe(child));
							return;
						}
					}
					LOG.trace("Stepping {}", step);
					uiController.loopMainThreadForAtLeast(step);
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

	public static ActivityResult cancelResult() {
		return new ActivityResult(Activity.RESULT_CANCELED, null);
	}

	public static ViewAction rotateActivity() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return any(View.class);
			}
			@Override public String getDescription() {
				return "rotate activity to opposite orientation";
			}
			@Override public void perform(UiController uiController, View view) {
				Activity activity = findActivity(view.getContext());
				if (activity == null) {
					LOG.warn("No activity from {}, using topmost resumed activity.", HumanReadables.describe(view));
					activity = InstrumentationExtensions.tryGetActivityInStage(Stage.RESUMED);
				}
				if (activity == null) {
					throw new IllegalStateException("No activity can be found from " + HumanReadables.describe(view));
				}
				//noinspection WrongConstant it's the right one
				activity.setRequestedOrientation(getOppositeOrientation(activity));
			}
			private int getOppositeOrientation(Activity activity) {
				int orientation = activity.getResources().getConfiguration().orientation;
				int request;
				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					request = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				} else {
					request = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				}
				return request;
			}
			private Activity findActivity(Context context) {
				while (context instanceof ContextWrapper) {
					if (context instanceof Activity) {
						return (Activity)context;
					}
					context = ((ContextWrapper)context).getBaseContext();
				}
				return null;
			}
		};
	}

	public static Matcher<Intent> chooser(Matcher<Intent> matcher) {
		return allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(is(Intent.EXTRA_INTENT), matcher));
	}

	/**
	 * @see <a href="https://code.google.com/p/android/issues/detail?id=199544">
	 * perform(click()) is doing a long press randomly and causing tests to fail</a>
	 */
	private static void openActionBarOverflowOrOptionsMenuWithOversleepProtection() {
		// to prevent overflow button click from oversleeping and clicking the first item in the popup via a longClick
		// the Touch & Hold Delay (aka longClick()) has to be set to at least Medium for this to be less flaky.
		// On Nexus 5 SDK emulator Short is 30%, Medium is 20%, Long is 5% flaky.

		// An extra loop until idle helps to make them less flaky by itself, even though each perform does that anyway.
		onRoot().perform(loopMainThreadUntilIdle());

		// Look for "Overslept and turned a tap into a long press" to detect failure early, otherwise
		// a NoMatchingRootException will be thrown which is hard to decode and separated by at least ~30 lines of logs.
		long oversleepBarrier = System.currentTimeMillis();
		openActionBarOverflowOrOptionsMenu(getTargetContext());
		if (hasOversleptLogMessageAfter(oversleepBarrier)) {
			throw new FlakyTestException(OVERSLEEP_TAG + ": " + OVERSLEEP_MESSAGE);
		}

		// TODO this is done by default by Espresso 3.x?
		// wait for a platform popup to become available as root, this is the action bar overflow menu popup
		for (long waitTime : new long[] {10, 50, 100, 500, 1000, 3000, 5000}) { // ~10 seconds altogether
			try {
				onRoot(isPopupMenu()).check(matches(anything()));
				break;
			} catch (NoMatchingRootException ex) {
				LOG.warn("No popup menu is available - waiting: " + waitTime + "ms for one to appear.");
				onRoot().perform(loopMainThreadForAtLeast(waitTime));
			}
		}
		// double-check the root is available, if test fails here it means the popup didn't show up
		onRoot(isPopupMenu()).check(matches(isCompletelyDisplayed()));
		// check that the current default root is the popup
		onRoot().check(matches(root(isPopupMenu())));
	}

	public static DataInteraction onActionMenuItem(Matcher<MenuItem> matcher) {
		openActionBarOverflowOrOptionsMenuWithOversleepProtection();
		return onData(matcher);
	}

	public static ViewInteraction onActionMenuView(Matcher<View> matcher) {
		openActionBarOverflowOrOptionsMenuWithOversleepProtection();
		return onView(matcher);//.inRoot(isPlatformPopup()); // not needed as the popup is topmost & focused
	}

	private static boolean hasOversleptLogMessageAfter(long oversleepBarrier) {
		try {
			// -d: dump and stop, don't block; -b main: app's logs; -v time: output format
			// -s: silent by default; MotionEvents:E: only interested in lines starting with E/MotionEvents
			Process process = Runtime.getRuntime().exec("logcat -d -b main -v time -s " + OVERSLEEP_TAG + ":E");
			BufferedReader pipe = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String lastLine = null;
			String line;
			while ((line = pipe.readLine()) != null) {
				LOG.trace(line);
				if (line.contains(OVERSLEEP_MESSAGE)) {
					lastLine = line;
				}
			}
			pipe.close();
			if (lastLine != null && lastLine.contains(String.valueOf(android.os.Process.myPid()))) {
				// -v time formats the log like this:
				// 09-09 18:20:25.539 E/MotionEvents( 4441): Overslept and turned a tap into a long press
				int year = Calendar.getInstance().get(Calendar.YEAR);
				Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ROOT).parse(year + "-" + lastLine);
				if (oversleepBarrier < parse.getTime()) {
					// pretty sure it happened "just now"
					return true;
				}
			}
		} catch (Exception /*IOException | ParseException*/ ex) {
			// oh well, let's continue and we'll see what life gets us: it should throw/fail something later
			LOG.warn("Cannot get last logcat line to detect MotionEvents oversleeping.", ex);
		}
		return false;
	}

	public static ViewInteraction onActionBarDescendant(Matcher<View> viewMatcher) {
		return onView(allOf(viewMatcher, inActionBar()));
	}
	public static Matcher<View> inActionBar() {
		return isDescendantOfA(isActionBar());
	}
	private static Matcher<View> isActionBar() {
		return withFullResourceName(endsWith(":id/action_bar"));
	}
	public static Matcher<View> inContextualActionBar() {
		return isDescendantOfA(isContextualActionBar());
	}
	public static Matcher<View> isContextualActionBar() {
		return withFullResourceName(endsWith(":id/action_context_bar"));
	}
	public static Matcher<View> isActionBarTitle() {
		return allOf(inActionBar(), isToolbarTitle());
	}
	public static Matcher<View> isActionBarSubTitle() {
		return allOf(inActionBar(), isToolbarSubTitle());
	}

	public static Matcher<View> inToolbar() {
		return isDescendantOfA(isToolbar());
	}
	private static Matcher<View> isToolbar() {
		return isAssignableFrom(android.support.v7.widget.Toolbar.class);
	}
	public static Matcher<View> isToolbarTitle() {
		return new ReflectiveParentViewMatcher(isToolbar(), "mTitleTextView");
	}
	public static Matcher<View> isToolbarSubTitle() {
		return new ReflectiveParentViewMatcher(isToolbar(), "mSubtitleTextView");
	}

	public static @NonNull Matcher<MenuItem> withMenuItemId(@IdRes int viewId) {
		return withMenuItemIdAndroid(viewId);
	}

	private static @NonNull Matcher<MenuItem> withMenuItemIdAndroid(@IdRes final int menuId) {
		final Matcher<Integer> viewIdMatcher = equalTo(menuId);
		return new TypeSafeMatcher<MenuItem>(MenuItem.class) {

			private final Resources resources = InstrumentationRegistry.getTargetContext().getResources();

			@Override public void describeTo(Description description) {
				description.appendText("with menu item id: " + getIdDescription(resources, menuId));
			}

			@Override protected boolean matchesSafely(MenuItem item) {
				return viewIdMatcher.matches(item.getItemId());
			}
		};
	}

	private static @NonNull Matcher<View> withMenuItemIdCompat(@IdRes final int menuId) {
		final Matcher<Integer> viewIdMatcher = equalTo(menuId);
		// simplified version of Espresso's ViewMatchers.WithIdMatcher
		return new BoundedMatcher<View, View>(View.class, MenuView.ItemView.class) {

			private Resources resources = InstrumentationRegistry.getTargetContext().getResources();

			@Override public void describeTo(Description description) {
				description.appendText("with menu item id: " + getIdDescription(resources, menuId));
			}

			@Override public boolean matchesSafely(View view) {
				int id = ((MenuView.ItemView)view).getItemData().getItemId();
				resources = view.getResources();
				return viewIdMatcher.matches(id);
			}
		};
	}

	/**
	 * Adapted from Espresso's ViewMatchers.WithIdMatcher.
	 */
	private static @NonNull String getIdDescription(@Nullable Resources resources, @IdRes int id) {
		String idDescription;
		if (resources != null) {
			try {
				idDescription = resources.getResourceName(id);
			} catch (Resources.NotFoundException e) {
				idDescription = String.format(Locale.ROOT, "%1$08x [%1$d] (resource name not found)", id);
			}
		} else {
			idDescription = String.format(Locale.ROOT, "%1$08x [%1$d]", id);
		}
		return idDescription;
	}

	public static Matcher<View> withFullResourceName(String resourceName) {
		return withFullResourceName(is(resourceName));
	}

	public static Matcher<View> withFullResourceName(final Matcher<String> resourceNameMatcher) {
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

	public static Matcher<View> isRV() {
		return isAssignableFrom(RecyclerView.class);
	}

	public static ViewAssertion itemDoesNotExists(Matcher<View> dataMatcher) {
		return matches(not(withAdaptedView(dataMatcher)));
	}

	/**
	 * For a negative match (i.e. check for non-existent data), use
	 * {@code onView(isRV()).check(itemDoesNotExists(withText(TEST_ROOM)))}
	 * @see Espresso#onData(Matcher)
	 * @see #withAdaptedView(Matcher)
	 * @see #itemDoesNotExists(Matcher)
	 */
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

	/**
	 * Positive match: {@code onView(withId(R.id.list)).check(matches(withAdaptedData(...)))},
	 * but {@link #onRecyclerItem(Matcher)} is a better approach
	 * Negative match: {@code onView(withId(R.id.list)).check(matches(not(withAdaptedData(...))))}
	 * @see <a href="https://google.github.io/android-testing-support-library/docs/espresso/advanced/#asserting-that-a-data-item-is-not-in-an-adapter">
	 *     Asserting that a data item is not in an adapter</a>
	 */
	public static Matcher<View> withAdaptedView(final Matcher<View> dataMatcher) {
		return new TypeSafeMatcher<View>() {
			@Override public void describeTo(Description description) {
				description.appendText("with adapted view: ").appendDescriptionOf(dataMatcher);
			}

			@Override public boolean matchesSafely(View view) {
				return view instanceof RecyclerView && hasBoundView(((RecyclerView)view));
			}
			private <T extends RecyclerView.ViewHolder> boolean hasBoundView(RecyclerView rv) {
				@SuppressWarnings("unchecked")
				RecyclerView.Adapter<T> adapter = (RecyclerView.Adapter<T>)rv.getAdapter();
				for (int i = 0; i < adapter.getItemCount(); i++) {
					int type = adapter.getItemViewType(i);
					T viewHolder = adapter.createViewHolder(rv, type);
					adapter.bindViewHolder(viewHolder, i);
					if (dataMatcher.matches(viewHolder.itemView)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * @see <a href="https://google.github.io/android-testing-support-library/docs/espresso/advanced/#asserting-that-a-data-item-is-not-in-an-adapter">
	 *     Asserting that a data item is not in an adapter</a>
	 */
	public static Matcher<View> withAdaptedData(final Matcher<?> dataMatcher) {
		return new TypeSafeMatcher<View>() {
			@Override public void describeTo(Description description) {
				description.appendText("with adapted data: ");
				dataMatcher.describeTo(description);
			}

			@Override public boolean matchesSafely(View view) {
				return view instanceof AdapterView && hasBoundView((AdapterView<?>)view);
			}
			private boolean hasBoundView(AdapterView<?> view) {
				Adapter adapter = view.getAdapter();
				for (int i = 0; i < adapter.getCount(); i++) {
					if (dataMatcher.matches(adapter.getItem(i))) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Run it in debug: {@code com.google.common.collect.Iterators.toArray(net.twisterrob.android.test.espresso.EspressoExtensions.parentViewTraversal(view).iterator(), android.view.View.class)}.
	 */
	public static Iterable<View> parentViewTraversal(final View view) {
		return new Iterable<View>() {
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override public Iterator<View> iterator() {
				Predicate<Object> isView = new Predicate<Object>() {
					@Override public boolean apply(Object input) {
						return View.class.isInstance(input);
					}
				};
				return (Iterator<View>)(Iterator)Iterators.filter(parentTraversal(view).iterator(), isView);
			}
		};
	}
	public static ViewAction scrollToLast() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isScrollPositionAware();
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
	public static ViewAction jumpToLast() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isScrollPositionAware();
			}
			@Override public String getDescription() {
				return "scroll to last item";
			}
			@Override public void perform(UiController uiController, View view) {
				if (view instanceof AbsListView) {
					AbsListView adapterView = (AbsListView)view;
					adapterView.setSelection(adapterView.getAdapter().getCount() - 1);
				} else if (view instanceof RecyclerView) {
					RecyclerView recycler = (RecyclerView)view;
					recycler.scrollToPosition(recycler.getAdapter().getItemCount() - 1);
				} else if (view instanceof ViewPager) {
					ViewPager pager = (ViewPager)view;
					pager.setCurrentItem(pager.getAdapter().getCount() - 1, false);
				}
			}
		};
	}

	private static Matcher<View> isScrollPositionAware() {
		return anyOf(
				allOf(isAssignableFrom(AbsListView.class), hasAdapter()),
				allOf(isAssignableFrom(RecyclerView.class), hasRecyclerAdapter()),
				allOf(isAssignableFrom(ViewPager.class), hasViewPagerAdapter())
		);
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

	public static Matcher<View> hasImage() {
		return new BoundedMatcher<View, ImageView>(ImageView.class) {
			@Override public void describeTo(Description description) {
				description.appendText("ImageView has image");
			}
			@Override protected boolean matchesSafely(ImageView item) {
				return item.getDrawable() != null;
			}
		};
	}
}
