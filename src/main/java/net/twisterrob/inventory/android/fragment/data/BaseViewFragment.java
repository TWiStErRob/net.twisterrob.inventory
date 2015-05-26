package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.ImageActivity;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.view.ChangeTypeListener;

public abstract class BaseViewFragment<DTO extends ImagedDTO, T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseViewFragment.class);

	protected ViewPager pager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_details, container, false);
	}

	@Override public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		pager = (ViewPager)view.findViewById(R.id.pager);
	}

	public void onSingleRowLoaded(DTO entity) {
		pager.setAdapter(new ImageAndDescriptionAdapter(entity));
		pager.setCurrentItem(getDefaultPageIndex());
	}

	private int getDefaultPageIndex() {
		String key = getString(R.string.pref_defaultEntityDetailsPage);
		String defaultValue = getString(R.string.pref_defaultEntityDetailsPage_default);
		String defaultPage = App.getPrefs().getString(key, defaultValue);
		return AndroidTools.findIndexInResourceArray(getContext(),
				R.array.pref_defaultEntityDetailsPage_values, defaultPage);
	}
	protected abstract CharSequence getDetailsString(DTO entity, boolean DEBUG);

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
			View view = (View)object;
			if (view.findViewById(R.id.image) != null) {
				return 0;
			} else if (view.findViewById(R.id.details) != null) {
				return 1;
			}
			return -1;
		}

		@Override
		public Object instantiateItem(final ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View view;
			switch (position) {
				case 0: {
					view = inflater.inflate(R.layout.inc_details_image, container, false);
					ImageView image = (ImageView)view.findViewById(R.id.image);
					ImageView type = (ImageView)view.findViewById(R.id.type);
					AndroidTools.visibleIf(type, !(entity instanceof CategoryDTO));

					if (!(entity instanceof CategoryDTO)) {
						image.setOnClickListener(new ImageOpenListener());
						image.setOnLongClickListener(new ImageChangeListener());
						type.setOnClickListener(new ChangeTypeListener(BaseViewFragment.this, entity));
					}

					entity.loadInto(image, type, true);
					break;
				}
				case 1: {
					view = inflater.inflate(R.layout.inc_details_details, container, false);

					final boolean debug =
							App.getBPref(R.string.pref_displayDebugDetails, R.bool.pref_displayDebugDetails_default);

					TextView details = (TextView)view.findViewById(R.id.details);
					details.setText(getDetailsString(entity, debug));
					//details.setMovementMethod(ScrollingMovementMethod.getInstance());
					//details.setOnTouchListener(new DeepScrollFixListener());
					break;
				}
				default:
					throw new UnsupportedOperationException("Position #" + position + " is not supported");
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

		private class ImageOpenListener implements OnClickListener {
			@Override public void onClick(View v) {
				if (entity.hasImage) {
					startActivity(ImageActivity.show(entity.getImageUri()));
				} else {
					editImage();
				}
			}
		}

		private class ImageChangeListener implements OnLongClickListener {
			@Override public boolean onLongClick(View v) {
				editImage();
				return true;
			}
		}
	}

	protected abstract void editImage();
}
