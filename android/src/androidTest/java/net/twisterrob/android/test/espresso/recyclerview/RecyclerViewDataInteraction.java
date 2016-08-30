package net.twisterrob.android.test.espresso.recyclerview;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.support.test.espresso.*;
import android.support.test.espresso.core.deps.guava.base.Optional;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewProtocol.AdaptedData;

/**
 * An interface to interact with data displayed in AdapterViews.
 * <p>
 * This interface builds on top of {@link ViewInteraction} and should be the preferred way to
 * interact with elements displayed inside AdapterViews.
 * </p>
 * <p>
 * This is necessary because an AdapterView may not load all the data held by its Adapter into the
 * view hierarchy until a user interaction makes it necessary. Also it is more fluent / less brittle
 * to match upon the data object being rendered into the display then the rendering itself.
 * </p>
 * <p>
 * By default, a RecyclerViewDataInteraction takes place against any AdapterView found within the current
 * screen, if you have multiple AdapterView objects displayed, you will need to narrow the selection
 * by using the inAdapterView method.
 * </p>
 * <p>
 * The check and perform method operate on the top level child of the adapter view, if you need to
 * operate on a subview (eg: a Button within the list) use the onChildView method before calling
 * perform or check.
 * </p>
 *
 * @see android.support.test.espresso.DataInteraction original where this is copied from
 */
public class RecyclerViewDataInteraction {
	private final Matcher<? extends Object> dataMatcher;
	private Matcher<View> adapterMatcher = isAssignableFrom(RecyclerView.class);
	private Optional<Matcher<View>> childViewMatcher = Optional.absent();
	private Optional<Integer> atPosition = Optional.absent();
	private RecyclerViewProtocol adapterViewProtocol = RecyclerViewProtocols.standardProtocol();
	private Matcher<Root> rootMatcher = RootMatchers.DEFAULT;

	public RecyclerViewDataInteraction(Matcher<View> dataMatcher) {
		this.dataMatcher = viewHolderMatcher(dataMatcher);
	}
	/**
	 * Creates matcher for view holder with given item view matcher.
	 *
	 * @param itemViewMatcher a item view matcher which is used to match item.
	 * @return a matcher which matches a view holder containing item matching itemViewMatcher.
	 */
	private static <VH extends ViewHolder> Matcher<VH> viewHolderMatcher(
			final Matcher<View> itemViewMatcher) {
		return new TypeSafeMatcher<VH>() {
			@Override
			public boolean matchesSafely(RecyclerView.ViewHolder viewHolder) {
				return itemViewMatcher.matches(viewHolder.itemView);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("holder with view: ");
				itemViewMatcher.describeTo(description);
			}
		};
	}

	/**
	 * Causes perform and check methods to take place on a specific child view of the view returned
	 * by Adapter.getView()
	 */
	public RecyclerViewDataInteraction onChildView(Matcher<View> childMatcher) {
		this.childViewMatcher = Optional.of(checkNotNull(childMatcher));
		return this;
	}

	/**
	 * Causes this data interaction to work within the Root specified by the given root matcher.
	 */
	public RecyclerViewDataInteraction inRoot(Matcher<Root> rootMatcher) {
		this.rootMatcher = checkNotNull(rootMatcher);
		return this;
	}

	/**
	 * Selects a particular adapter view to operate on, by default we operate on any adapter view
	 * on the screen.
	 */
	public RecyclerViewDataInteraction inAdapterView(Matcher<View> adapterMatcher) {
		this.adapterMatcher = checkNotNull(adapterMatcher);
		return this;
	}

	/**
	 * Selects the view which matches the nth position on the adapter
	 * based on the data matcher.
	 */
	public RecyclerViewDataInteraction atPosition(Integer atPosition) {
		this.atPosition = Optional.of(checkNotNull(atPosition));
		return this;
	}

	/**
	 * Use a different AdapterViewProtocol if the Adapter implementation does not
	 * satisfy the AdapterView contract like (@code ExpandableListView)
	 */
	public RecyclerViewDataInteraction usingAdapterViewProtocol(RecyclerViewProtocol adapterViewProtocol) {
		this.adapterViewProtocol = checkNotNull(adapterViewProtocol);
		return this;
	}

	/**
	 * Performs an action on the view after we force the data to be loaded.
	 *
	 * @return an {@link ViewInteraction} for more assertions or actions.
	 */
	public ViewInteraction perform(ViewAction... actions) {
		RecyclerViewDataLoaderAction adapterDataLoaderAction = load();

		return onView(makeTargetMatcher(adapterDataLoaderAction))
				.inRoot(rootMatcher)
				.perform(actions);
	}

	/**
	 * Performs an assertion on the state of the view after we force the data to be loaded.
	 *
	 * @return an {@link ViewInteraction} for more assertions or actions.
	 */
	public ViewInteraction check(ViewAssertion assertion) {
		RecyclerViewDataLoaderAction adapterDataLoaderAction = load();

		return onView(makeTargetMatcher(adapterDataLoaderAction))
				.inRoot(rootMatcher)
				.check(assertion);
	}

	private RecyclerViewDataLoaderAction load() {
		RecyclerViewDataLoaderAction adapterDataLoaderAction =
				new RecyclerViewDataLoaderAction(dataMatcher, atPosition, adapterViewProtocol);
		onView(adapterMatcher)
				.inRoot(rootMatcher)
				.perform(adapterDataLoaderAction);
		return adapterDataLoaderAction;
	}

	@SuppressWarnings("unchecked")
	private Matcher<View> makeTargetMatcher(RecyclerViewDataLoaderAction adapterDataLoaderAction) {
		Matcher<View> targetView = displayingData(adapterMatcher, dataMatcher, adapterViewProtocol,
				adapterDataLoaderAction);
		if (childViewMatcher.isPresent()) {
			targetView = allOf(childViewMatcher.get(), isDescendantOfA(targetView));
		}
		return targetView;
	}

	private Matcher<View> displayingData(
			final Matcher<View> adapterMatcher,
			final Matcher<? extends Object> dataMatcher,
			final RecyclerViewProtocol adapterViewProtocol,
			final RecyclerViewDataLoaderAction adapterDataLoaderAction) {
		checkNotNull(adapterMatcher);
		checkNotNull(dataMatcher);
		checkNotNull(adapterViewProtocol);

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText(" displaying data matching: ");
				dataMatcher.describeTo(description);
				description.appendText(" within adapter view matching: ");
				adapterMatcher.describeTo(description);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean matchesSafely(View view) {

				ViewParent parent = view.getParent();

				while (parent != null && !(parent instanceof RecyclerView)) {
					parent = parent.getParent();
				}

				if (parent != null && adapterMatcher.matches(parent)) {
					Optional<AdaptedData> data = adapterViewProtocol.getDataRenderedByView(
							(RecyclerView)parent, view);
					if (data.isPresent()) {
						return adapterDataLoaderAction.getAdaptedData().opaqueToken.equals(
								data.get().opaqueToken);
					}
				}
				return false;
			}
		};
	}
}
