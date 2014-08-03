package net.twisterrob.inventory.android.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;

public abstract class BaseViewFragment<T> extends BaseSingleLoaderFragment<T> {
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
}
