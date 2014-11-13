package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.LoadSingleRow;

public abstract class BaseSingleLoaderFragment<T> extends BaseFragment<T> {
	protected abstract void onSingleRowLoaded(Cursor cursor);

	/** <code>getLoaderManager().initLoader(Loaders.X.ordinal(), args, new SingleRowLoaded());</code> */
	protected class SingleRowLoaded extends LoadSingleRow {
		public SingleRowLoaded() {
			super(getContext());
		}

		@Override
		protected void process(Cursor cursor) {
			super.process(cursor);
			onSingleRowLoaded(cursor);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}
}
