package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;

import androidx.annotation.*;

import net.twisterrob.inventory.android.content.LoadSingleRow;

public abstract class BaseSingleLoaderFragment<T> extends BaseFragment<T> {
	protected abstract void onSingleRowLoaded(@NonNull Cursor cursor);

	/** <code>getLoaderManager().initLoader(Loaders.X.getID(0), args,
	 *        Loaders.X.createCallbacks(getContext(), new SingleRowLoaded());</code>
	 */
	protected class SingleRowLoaded extends LoadSingleRow {
		public SingleRowLoaded() {
		}

		@Override protected void process(@NonNull Cursor cursor) {
			super.process(cursor);
			onSingleRowLoaded(cursor);
		}

		@Override protected void processInvalid(@Nullable Cursor item) {
			super.processInvalid(item);
			requireActivity().finish();
		}
	}
}
