package net.twisterrob.inventory.android.fragment.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;

import static net.twisterrob.inventory.android.Constants.Dimensions.*;
import static net.twisterrob.inventory.android.Constants.Prefs.*;

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
		getBaseActivity().setActionBarTitle(entity.name);
		getBaseActivity().setIcon(entity.getFallbackDrawable(getContext(),
				getActionbarIconSize(getContext()), getActionbarIconPadding(getContext())));
		pager.setAdapter(new ImageAndDescriptionAdapter(entity));
		pager.setCurrentItem(getDefaultPageIndex());
	}

	private int getDefaultPageIndex() {
		String defaultPage = App.getPrefs().getString(DEFAULT_ENTITY_DETAILS_PAGE, DEFAULT_ENTITY_DETAILS_PAGE_DEFAULT);
		return AndroidTools.findIndexInResourceArray(getContext(),
				R.array.pref_defaultEntityDetailsPage_values, defaultPage);
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
			Context context = image.getContext();
			Drawable fallback = entity.getFallbackDrawable(context);
			String imagePath = entity.getImage(context);
			if (imagePath != null) {
				App.pic().start(BaseViewFragment.this).placeholder(fallback).load(imagePath).into(image);
			} else {
				image.setImageDrawable(fallback);
			}
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
