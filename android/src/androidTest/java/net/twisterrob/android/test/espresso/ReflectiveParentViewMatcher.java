package net.twisterrob.android.test.espresso;

import org.hamcrest.*;

import android.support.test.espresso.util.HumanReadables;
import android.view.View;

import net.twisterrob.java.utils.ReflectionTools;

public class ReflectiveParentViewMatcher extends TypeSafeDiagnosingMatcher<View> {
	private final Matcher<View> parentMatcher;
	private final String viewFieldName;

	public ReflectiveParentViewMatcher(Matcher<View> parentMatcher, String viewFieldName) {
		this.parentMatcher = parentMatcher;
		this.viewFieldName = viewFieldName;
	}

	@Override protected boolean matchesSafely(View view, Description mismatchDescription) {
		View parent = findParentToReflectOn(view);
		if (parent == null) {
			mismatchDescription.appendText("Cannot find parent ").appendDescriptionOf(parentMatcher);
			return false;
		}
		Object field = ReflectionTools.get(parent, viewFieldName);
		if (field == null) {
			mismatchDescription
					.appendText("Cannot find field ").appendValue(viewFieldName)
					.appendText(" in ")
					.appendValue(HumanReadables.describe(parent));
			return false;
		}
		return view == field;
	}

	protected View findParentToReflectOn(View view) {
		for (View parent : EspressoExtensions.parentViewTraversal(view)) {
			if (parentMatcher.matches(parent)) {
				return parent;
			}
		}
		return null;
	}

	@Override public void describeTo(Description description) {
		description
				.appendText("same view as ").appendValue(viewFieldName)
				.appendText(" in ").appendDescriptionOf(parentMatcher);
	}
}
