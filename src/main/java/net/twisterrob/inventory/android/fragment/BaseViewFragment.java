package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.LoadSingleRow;

public abstract class BaseViewFragment<T> extends BaseFragment<T> {
	protected TextView title;
	protected TextView type;
	protected ImageView image;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.details, container, false);
		title = (TextView)root.findViewById(R.id.title);
		type = (TextView)root.findViewById(R.id.type);
		image = (ImageView)root.findViewById(R.id.image);
		return root;
	}

	protected class SingleRowLoaded extends LoadSingleRow {
		public SingleRowLoaded() {
			super(getActivity());
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

	protected abstract void onSingleRowLoaded(Cursor cursor);
}
