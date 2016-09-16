/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.twisterrob.android.test.espresso.recyclerview;

import java.util.List;

import android.database.Cursor;
import android.support.test.espresso.core.deps.guava.base.Optional;
import android.support.test.espresso.core.deps.guava.collect.*;
import android.support.v7.widget.*;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

/**
 * Implementations of {@link RecyclerViewProtocol} for standard SDK Widgets.
 *
 * @see android.support.test.espresso.action.AdapterViewProtocols original where this is copied from
 */
public final class RecyclerViewProtocols {

	/**
	 * Consider views which have over this percentage of their area visible to the user
	 * to be fully rendered.
	 */
	private static final int FULLY_RENDERED_PERCENTAGE_CUTOFF = 100;

	private RecyclerViewProtocols() {
	}

	private static final RecyclerViewProtocol STANDARD_PROTOCOL = new StandardRecyclerViewProtocol();

	/**
	 * Creates an implementation of RecyclerViewProtocol that can work with AdapterViews that do not
	 * break method contracts on AdapterView.
	 *
	 */
	public static RecyclerViewProtocol standardProtocol() {
		return STANDARD_PROTOCOL;
	}

	private static final class StandardRecyclerViewProtocol implements RecyclerViewProtocol {

		private static final String TAG = "StdRecyclerViewProtocol";

		private static final class StandardDataFunction implements DataFunction {
			private final Object dataAtPosition;
			private final int position;

			private StandardDataFunction(Object dataAtPosition, int position) {
				checkArgument(position >= 0, "position must be >= 0");
				this.dataAtPosition = dataAtPosition;
				this.position = position;
			}

			@Override
			public Object getData() {
				if (dataAtPosition instanceof Cursor) {
					if (!((Cursor)dataAtPosition).moveToPosition(position)) {
						Log.e(TAG, "Cannot move cursor to position: " + position);
					}
				}
				return dataAtPosition;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterable<AdaptedData> getDataInAdapterView(RecyclerView adapterView) {
			List<AdaptedData> datas = Lists.newArrayList();
			for (int position = 0; position < adapterView.getAdapter().getItemCount(); position++) {
				RecyclerView.ViewHolder dataAtPosition = adapterView.findViewHolderForAdapterPosition(position);
				if (dataAtPosition == null) {
					int itemType = adapterView.getAdapter().getItemViewType(position);
					dataAtPosition = adapterView.getAdapter().createViewHolder(adapterView, itemType);
					adapterView.getAdapter().bindViewHolder(dataAtPosition, position);
				}
				datas.add(
						new AdaptedData.Builder()
								.withDataFunction(new StandardDataFunction(dataAtPosition, position))
								.withOpaqueToken(position)
								.build());
			}
			return datas;
		}

		@Override
		public Optional<AdaptedData> getDataRenderedByView(RecyclerView adapterView,
				View descendantView) {
			if (adapterView == descendantView.getParent()) {
				int position = adapterView.getChildAdapterPosition(descendantView);
				if (position != AdapterView.INVALID_POSITION) {
					return Optional.of(new AdaptedData.Builder()
							.withDataFunction(
									new StandardDataFunction(adapterView.findViewHolderForAdapterPosition(position),
											position))
							.withOpaqueToken(position)
							.build());
				}
			}
			return Optional.absent();
		}

		@Override
		public void makeDataRenderedWithinAdapterView(
				RecyclerView adapterView, AdaptedData data) {
			checkArgument(data.opaqueToken instanceof Integer, "Not my data: %s", data);
			int position = (Integer)data.opaqueToken;
			adapterView.smoothScrollToPosition(position);
		}

		@Override
		public boolean isDataRenderedWithinAdapterView(
				RecyclerView adapterView, AdaptedData adaptedData) {
			checkArgument(adaptedData.opaqueToken instanceof Integer, "Not my data: %s", adaptedData);
			int dataPosition = (Integer)adaptedData.opaqueToken;
			boolean inView = false;

			LayoutManager manager = adapterView.getLayoutManager();
			if (manager instanceof LinearLayoutManager) {
				LinearLayoutManager linear = (LinearLayoutManager)manager;
				if (Range.closed(linear.findFirstVisibleItemPosition(), linear.findLastVisibleItemPosition())
				         .contains(dataPosition)) {
					if (linear.findFirstVisibleItemPosition() == linear.findLastVisibleItemPosition()) {
						// that's a huge element.
						inView = true;
					} else {
						inView = isElementFullyRendered(adapterView, dataPosition);
					}
				}
			}
			if (inView) {
				// stops animations - locks in our x/y location.
				adapterView.stopScroll();
			}

			return inView;
		}

		private boolean isElementFullyRendered(RecyclerView adapterView,
				int childAt) {
			View element = adapterView.getLayoutManager().findViewByPosition(childAt);
			// Occasionally we'll have to fight with smooth scrolling logic on our definition of when
			// there is extra scrolling to be done. In particular if the element is the first or last
			// element of the list, the smooth scroller may decide that no work needs to be done to scroll
			// to the element if a certain percentage of it is on screen. Ugh. Sigh. Yuck.

			return isDisplayingAtLeast(FULLY_RENDERED_PERCENTAGE_CUTOFF).matches(element);
		}
	}
}
