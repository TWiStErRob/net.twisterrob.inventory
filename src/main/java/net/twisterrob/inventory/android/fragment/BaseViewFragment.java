package net.twisterrob.inventory.android.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;

import net.twisterrob.inventory.R;

public abstract class BaseViewFragment<T> extends BaseSingleLoaderFragment<T> {
	protected ImageView image;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.details, container, false);
		image = (ImageView)root.findViewById(R.id.image);
		return root;
	}
}
