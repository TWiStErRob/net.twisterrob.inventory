package net.twisterrob.android.test.espresso;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.support.test.espresso.*;
import android.support.test.espresso.util.HumanReadables;
import android.support.v4.view.*;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.contrib.DrawerActions.*;
import static android.support.test.espresso.contrib.DrawerMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.annotation.GravityFlag;

public class DrawerMatchers {
	public static ViewInteraction onDrawerDescendant(Matcher<View> viewMatcher) {
		return onView(allOf(viewMatcher, inDrawer())).perform(openContainingDrawer());
	}
	public static ViewAction openContainingDrawer() {
		return onContainingDrawer(openDrawerAction());
	}
	public static ViewAction closeContainingDrawer() {
		return onContainingDrawer(closeDrawerAction());
	}
	public static ViewAction openDrawerAction() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return allOf(isAnyDrawer(), withParent(not(isAnyOpen())));
			}
			@Override public String getDescription() {
				return "open the drawer";
			}
			@Override public void perform(UiController uiController, View drawer) {
				DrawerLayout layout = (DrawerLayout)drawer.getParent();
				layout.openDrawer(drawer);
			}
		};
	}
	public static ViewAction closeDrawerAction() {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return allOf(isAnyDrawer(), withParent(isAnyOpen()));
			}
			@Override public String getDescription() {
				return "close the drawer";
			}
			@Override public void perform(UiController uiController, View drawer) {
				DrawerLayout layout = (DrawerLayout)drawer.getParent();
				layout.closeDrawer(drawer);
			}
		};
	}
	public static ViewAction onContainingDrawer(final ViewAction drawerAction) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return inDrawer();
			}
			@Override public String getDescription() {
				return "open drawer the view is in";
			}
			@Override public void perform(UiController uiController, View view) {
				for (View drawer : EspressoExtensions.parentViewTraversal(view)) {
					Matcher<View> actionConstraints = drawerAction.getConstraints();
					Matcher<View> isDrawer = isDrawer();
					if (isDrawer.matches(drawer)) {
						if (actionConstraints.matches(drawer)) {
							drawerAction.perform(uiController, drawer);
							return;
						}
						throw new IllegalArgumentException(HumanReadables.describe(drawer)
								+ " matches " + isDrawer
								+ ", but doesn't match " + actionConstraints
								+ " from " + drawerAction
						);
					}
				}
				throw new IllegalStateException("Cannot find drawer parent");
			}
		};
	}
	public static ViewInteraction openDrawer() {
		return openDrawer(GravityCompat.START);
	}
	public static ViewInteraction openDrawer(@GravityFlag int gravity) {
		return onView(isDrawerLayout()).check(matches(isClosed(gravity))).perform(open(gravity));
	}
	public static ViewInteraction closeDrawer() {
		return closeDrawer(GravityCompat.START);
	}
	public static ViewInteraction closeDrawer(@GravityFlag int gravity) {
		return onView(isDrawerLayout()).check(matches(isOpen(gravity))).perform(close(gravity));
	}
	private static Matcher<View> isAnyOpen() {
		return anyOf(isOpen(GravityCompat.START), isOpen(GravityCompat.END));
	}
	/** Matches view containing the drawers and the contents. */
	public static Matcher<View> isDrawerLayout() {
		return isAssignableFrom(DrawerLayout.class);
	}
	/** Matches a usual left-side (RTL right-side) drawer view. */
	public static Matcher<View> isDrawer() {
		return isDrawerWithGravity(GravityCompat.START);
	}
	public static Matcher<View> isAnyDrawer() {
		return allOf(
				withParent(isDrawerLayout()),
				anyOf(hasDrawerGravity(GravityCompat.START), hasDrawerGravity(GravityCompat.END))
		);
	}
	/** Matches any view inside the usual left-side (RTL right-side) drawer view. */
	public static Matcher<View> inDrawer() {
		return isDescendantOfA(isDrawer());
	}
	public static Matcher<View> isDrawerContents() {
		return allOf(withParent(isDrawerLayout()),
				not(hasDrawerGravity(GravityCompat.START)),
				not(hasDrawerGravity(GravityCompat.END)));
	}
	public static Matcher<View> inDrawerContents() {
		return isDescendantOfA(isDrawerContents());
	}
	/** Matches the view that is on the {@code gravity} side of the drawer layout. */
	public static Matcher<View> isDrawerWithGravity(final @GravityFlag int gravity) {
		return allOf(withParent(isDrawerLayout()), hasDrawerGravity(gravity));
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
