package net.twisterrob.android.test.espresso;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.support.test.espresso.*;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.util.HumanReadables;
import android.support.v4.view.*;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.contrib.DrawerMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.v4.view.GravityCompat.*;

import net.twisterrob.android.annotation.GravityFlag;
import net.twisterrob.android.test.junit.InstrumentationExtensions;

/**
 * {@code onView(isDrawerLayout()).check(matches(isClosed(gravity))).perform(open(gravity));}
 */
public class DrawerMatchers {
	/** Overridable with {@code -e animateDrawers true} passed to instrumentation. */
	private static final boolean DEFAULT_DRAWER_ANIMATE =
			InstrumentationExtensions.getBooleanArgument("animateDrawers", false);

	public static ViewInteraction onDrawerDescendant(Matcher<View> viewMatcher) {
		return onDrawerDescendant(viewMatcher, DEFAULT_DRAWER_ANIMATE);
	}
	public static ViewInteraction onDrawerDescendant(Matcher<View> viewMatcher, boolean animate) {
		return onView(allOf(viewMatcher, inDrawer())).perform(openContainingDrawer(animate));
	}
	public static ViewAction openContainingDrawer() {
		return openContainingDrawer(DEFAULT_DRAWER_ANIMATE);
	}
	public static ViewAction openContainingDrawer(boolean animate) {
		return onContainingDrawer(openDrawer(animate));
	}
	public static ViewAction closeContainingDrawer() {
		return closeContainingDrawer(DEFAULT_DRAWER_ANIMATE);
	}
	public static ViewAction closeContainingDrawer(boolean animate) {
		return onContainingDrawer(closeDrawer(animate));
	}
	public static ViewAction openDrawer() {
		return openDrawer(DEFAULT_DRAWER_ANIMATE);
	}
	public static ViewAction openDrawer(final boolean animate) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return anyOf(isOpenableDrawer(Gravity.START), isOpenableDrawer(Gravity.END));
			}
			@Override public String getDescription() {
				return "open the drawer" + (animate? " (animated)" : "");
			}
			@Override public void perform(UiController uiController, View drawer) {
				DrawerLayout layout = (DrawerLayout)drawer.getParent();
				layout.openDrawer(drawer, animate);
			}
			private Matcher<View> isOpenableDrawer(@GravityFlag int gravity) {
				return allOf(isDrawer(gravity), withParent(allOf(
						isDrawerLayout(), not(isDrawerOpen(gravity)), not(isDrawerLocked(gravity)))));
			}
		};
	}
	public static ViewAction closeDrawer() {
		return closeDrawer(DEFAULT_DRAWER_ANIMATE);
	}
	public static ViewAction closeDrawer(final boolean animate) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return anyOf(isCloseableDrawer(START), isCloseableDrawer(END));
			}
			@Override public String getDescription() {
				return "close the drawer" + (animate? " (animated)" : "");
			}
			@Override public void perform(UiController uiController, View drawer) {
				DrawerLayout layout = (DrawerLayout)drawer.getParent();
				layout.closeDrawer(drawer, animate);
			}
			private Matcher<View> isCloseableDrawer(@GravityFlag int gravity) {
				return allOf(isDrawer(gravity), withParent(allOf(
						isDrawerLayout(), not(isDrawerClosed(gravity)), not(isDrawerLocked(gravity)))));
			}
		};
	}
	public static ViewAction onContainingDrawer(final ViewAction drawerAction) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return inDrawer();
			}
			@Override public String getDescription() {
				return drawerAction.getDescription() + " the view is in";
			}
			@Override public void perform(UiController uiController, View view) {
				Matcher<View> actionConstraints = drawerAction.getConstraints();
				Matcher<View> isDrawer = isDrawer();
				for (View drawer : EspressoExtensions.parentViewTraversal(view)) {
					if (isDrawer.matches(drawer)) {
						if (actionConstraints.matches(drawer)) {
							drawerAction.perform(uiController, drawer);
							return;
						}
						throw new IllegalArgumentException(HumanReadables.describe(drawer)
								+ " matches " + isDrawer
								+ ", but doesn't match " + actionConstraints
								+ " from " + drawerAction.getDescription()
						);
					}
				}
				throw new IllegalStateException("Cannot find drawer parent");
			}
		};
	}
	public static Matcher<View> isDrawerOpen(@GravityFlag int gravity) {
		return isOpen(gravity);
	}
	public static Matcher<View> isAnyDrawerOpen() {
		return anyOf(isStartDrawerOpen(), isEndDrawerOpen());
	}
	public static Matcher<View> isStartDrawerOpen() {
		return isOpen(START);
	}
	public static Matcher<View> isEndDrawerOpen() {
		return isOpen(END);
	}
	public static Matcher<View> areBothDrawersOpen() {
		return allOf(isStartDrawerOpen(), isEndDrawerOpen());
	}
	public static Matcher<View> isDrawerClosed(@GravityFlag int gravity) {
		return isClosed(gravity);
	}
	public static Matcher<View> isAnyDrawerClosed() {
		return anyOf(isStartDrawerClosed(), isEndDrawerClosed());
	}
	public static Matcher<View> isStartDrawerClosed() {
		return isClosed(START);
	}
	public static Matcher<View> isEndDrawerClosed() {
		return isClosed(END);
	}
	public static Matcher<View> areBothDrawersClosed() {
		return allOf(isStartDrawerClosed(), isEndDrawerClosed());
	}

	/** Matches view containing the drawers and the contents. */
	public static Matcher<View> isDrawerLayout() {
		return isAssignableFrom(DrawerLayout.class);
	}
	/** Matches a usual left-side (RTL right-side) drawer view. */
	public static Matcher<View> isStartDrawer() {
		return isDrawer(START);
	}
	/** Matches a usual left-side (RTL right-side) drawer view. */
	public static Matcher<View> isEndDrawer() {
		return isDrawer(START);
	}
	public static Matcher<View> inDrawer() {
		return isDescendantOfA(isDrawer());
	}
	/** Matches any view inside the drawer view, excluding the drawer view itself. */
	public static Matcher<View> inDrawer(@GravityFlag int gravity) {
		return isDescendantOfA(isDrawer(gravity));
	}
	public static Matcher<View> isDrawerContents() {
		return allOf(withParent(isDrawerLayout()),
				not(hasDrawerGravity(START)),
				not(hasDrawerGravity(END)));
	}
	public static Matcher<View> inDrawerContents() {
		return isDescendantOfA(isDrawerContents());
	}
	public static Matcher<View> isDrawer() {
		return allOf(withParent(isDrawerLayout()), anyOf(hasDrawerGravity(START), hasDrawerGravity(END)));
	}
	/** Matches the view that is on the {@code gravity} side of the drawer layout. */
	public static Matcher<View> isDrawer(final @GravityFlag int gravity) {
		return allOf(withParent(isDrawerLayout()), hasDrawerGravity(gravity));
	}
	private static Matcher<View> isDrawerLocked(final @GravityFlag int gravity) {
		if (!Gravity.isHorizontal(gravity) || Gravity.isVertical(gravity)) {
			throw new IllegalArgumentException(
					"Expected to match for a horizontal gravity, got " + GravityFlag.Converter.toString(gravity));
		}
		return new BoundedMatcher<View, DrawerLayout>(DrawerLayout.class) {
			@Override public void describeTo(Description description) {
				description.appendValue(GravityFlag.Converter.toString(gravity)).appendText(" drawer is locked");
			}
			@Override protected boolean matchesSafely(DrawerLayout item) {
				int mode = item.getDrawerLockMode(gravity);
				return mode == DrawerLayout.LOCK_MODE_LOCKED_OPEN || mode == DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
			}
		};
	}
	private static Matcher<View> hasDrawerGravity(final @GravityFlag int gravity) {
		if (!Gravity.isHorizontal(gravity) || Gravity.isVertical(gravity)) {
			throw new IllegalArgumentException(
					"Expected to match for a horizontal gravity, got " + GravityFlag.Converter.toString(gravity));
		}
		return new TypeSafeDiagnosingMatcher<View>(View.class) {
			@Override public void describeTo(Description description) {
				description.appendText("drawer gravity is ").appendValue(GravityFlag.Converter.toString(gravity));
			}
			@Override protected boolean matchesSafely(View child, Description mismatchDescription) {
				LayoutParams params = child.getLayoutParams();
				if (!(params instanceof DrawerLayout.LayoutParams)) {
					mismatchDescription.appendText("wrong params: " + params);
					return false;
				}
				DrawerLayout.LayoutParams drawerParams = (DrawerLayout.LayoutParams)params;
				int direction = ViewCompat.getLayoutDirection(child);
				int askedGravity = GravityCompat.getAbsoluteGravity(gravity, direction);
				int absGravity = GravityCompat.getAbsoluteGravity(drawerParams.gravity, direction);
				if ((absGravity & askedGravity) == 0) {
					mismatchDescription
							.appendText("drawer gravity of ")
							.appendValue(GravityFlag.Converter.toString(drawerParams.gravity))
							.appendText(" resolved as ")
							.appendValue(GravityFlag.Converter.toString(absGravity))
							.appendText(", but expected ")
							.appendValue(GravityFlag.Converter.toString(askedGravity))
					;
					return false;
				}
				return true;
			}
		};
	}
}
