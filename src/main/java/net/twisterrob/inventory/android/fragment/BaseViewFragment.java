package net.twisterrob.inventory.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public abstract class BaseViewFragment<DTO extends ImagedDTO, T> extends BaseSingleLoaderFragment<T> {
	protected ImageView image;
	protected ViewPager pager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.details, container, false);
		image = (ImageView)root.findViewById(R.id.image);
		pager = (ViewPager)root.findViewById(R.id.pager);
		return root;
	}

	public void onSingleRowLoaded(DTO entity) {
		setTitle(entity.name);
		setIcon(entity.getFallbackDrawable(getActivity()));
		pager.setAdapter(new ImageAndDescriptionAdapter(entity));
	}

	protected abstract CharSequence getDetailsString(DTO entity);

	private class ImageAndDescriptionAdapter extends PagerAdapter {
		private DTO entity;

		public ImageAndDescriptionAdapter(DTO imageData) {
			this.entity = imageData;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return position == 0? "Image" : "Description";
		}

		@Override
		public int getItemPosition(Object object) {
			return ((View)object).findViewById(R.id.image) != null? 0 : 1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			View view;
			if (position == 0) {
				view = inflater.inflate(R.layout.details_image, container, false);
				ImageView image = (ImageView)view.findViewById(R.id.image);
				entity.loadInto(image);
			} else {
				view = inflater.inflate(R.layout.details_description, container, false);
				TextView details = (TextView)view.findViewById(R.id.details);
				details.setText(getDetailsString(entity));
			}
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}
	}
}
