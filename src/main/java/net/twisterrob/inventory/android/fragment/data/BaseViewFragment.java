package net.twisterrob.inventory.android.fragment.data;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;

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
		pager.setCurrentItem(getDefaultPageIndex());
	}

	private int getDefaultPageIndex() {
		String defaultPage = App.getPrefs().getString(Prefs.DEFAULT_ENTITY_DETAILS_PAGE,
				Prefs.DEFAULT_ENTITY_DETAILS_PAGE_DEFAULT);
		int defaultIndex = AndroidTools.findIndexInResourceArray(getActivity(),
				R.array.pref_defaultEntityDetailsPage_values, defaultPage);
		return defaultIndex;
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
			return getResources().getTextArray(R.array.pref_defaultEntityDetailsPage_entries)[position];
		}

		@Override
		public int getItemPosition(Object object) {
			return ((View)object).findViewById(R.id.image) != null? 0 : 1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View view;
			if (position == 0) {
				view = inflater.inflate(R.layout.details_image, container, false);
				ImageView image = (ImageView)view.findViewById(R.id.image);
				loadInto(image);
			} else {
				view = inflater.inflate(R.layout.details_description, container, false);
				TextView details = (TextView)view.findViewById(R.id.details);
				details.setText(getDetailsString(entity));
			}
			container.addView(view);
			return view;
		}

		private void loadInto(ImageView image) {
			Drawable fallback = entity.getFallbackDrawable(image.getContext());
			App.pic().loadDrive(BaseViewFragment.this, entity.image).placeholder(fallback).into(image);
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
