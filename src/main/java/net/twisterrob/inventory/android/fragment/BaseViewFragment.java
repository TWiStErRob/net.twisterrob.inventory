package net.twisterrob.inventory.android.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;

public abstract class BaseViewFragment<T> extends BaseSingleLoaderFragment<T> {
	/**
	 * @deprecated should use a little icon in a corner like gallery_item does
	 */
	@Deprecated
	protected TextView type;
	protected ImageView image;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.details, container, false);
		type = (TextView)root.findViewById(R.id.type);
		image = (ImageView)root.findViewById(R.id.image);
		return root;
	}
}
