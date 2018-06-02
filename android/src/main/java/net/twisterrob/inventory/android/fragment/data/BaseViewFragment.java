package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.ImageActivity;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.view.ChangeTypeListener;

public abstract class BaseViewFragment<DTO extends ImagedDTO, T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseViewFragment.class);
	public static final String KEY_PAGE = "detailsPage";

	protected ViewPager pager;
	private Intent shareIntent;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_details, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		pager = (ViewPager)view.findViewById(R.id.pager);
	}

	public void onSingleRowLoaded(DTO entity) {
		ImageAndDescriptionAdapter adapter = new ImageAndDescriptionAdapter(entity);
		pager.setAdapter(adapter);
		pager.setCurrentItem(adapter.getPositionOf(getDefaultPosition()));
		shareIntent = entity.createShareIntent(getContext());
	}

	private String getDefaultPosition() {
		String auto = getString(R.string.pref_defaultViewPage_auto);
		String preference = App.prefs().getString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_default);
		String override = getArguments().getString(KEY_PAGE);
		String page = preference;
		if (auto.equals(page) && override != null) {
			page = override;
		}
		return page;
	}

	protected abstract CharSequence getDetailsString(DTO entity, boolean DEBUG);

	@Override public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		ViewTools.enabledIf(menu, R.id.action_share, shareIntent != null);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_share:
				// CONSIDER context sensitive sharing based on visible ViewPager page
				startActivity(shareIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class ImageAndDescriptionAdapter extends PagerAdapter {
		private static final int POSITION_UNKNOWN = -1;
		private static final int POSITION_IMAGE = 0;
		private static final int POSITION_DETAILS = 1;
		private static final int POSITION_COUNT = 2;

		private final DTO entity;

		public ImageAndDescriptionAdapter(DTO imageData) {
			this.entity = imageData;
		}

		@Override public int getCount() {
			return POSITION_COUNT;
		}

		@Override public CharSequence getPageTitle(int position) {
			return getResources().getTextArray(R.array.pref_defaultViewPage_entries)[position];
		}

		public int getPositionOf(String page) {
			String image = getString(R.string.pref_defaultViewPage_image);
			String details = getString(R.string.pref_defaultViewPage_details);
			if (image.equals(page)) {
				return POSITION_IMAGE;
			}
			if (details.equals(page)) {
				return POSITION_DETAILS;
			}
			return POSITION_UNKNOWN;
		}

		@Override public int getItemPosition(Object object) {
			View view = (View)object;
			if (view.findViewById(R.id.image) != null) {
				return POSITION_IMAGE;
			} else if (view.findViewById(R.id.details) != null) {
				return POSITION_DETAILS;
			}
			return POSITION_UNKNOWN;
		}

		@Override public Object instantiateItem(final ViewGroup container, int position) {
			LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View view;
			switch (position) {
				case POSITION_IMAGE: {
					view = inflater.inflate(R.layout.inc_details_image, container, false);
					ImageView image = (ImageView)view.findViewById(R.id.image);
					ImageView type = (ImageView)view.findViewById(R.id.type);
					ViewTools.visibleIf(type, !(entity instanceof CategoryDTO));

					if (!(entity instanceof CategoryDTO)) {
						image.setOnClickListener(new ImageOpenListener());
						image.setOnLongClickListener(new ImageChangeListener());
						type.setOnClickListener(new ChangeTypeListener(BaseViewFragment.this, entity));
					}

					entity.loadInto(image, type, true);
					break;
				}
				case POSITION_DETAILS: {
					view = inflater.inflate(R.layout.inc_details_details, container, false);

					final boolean debug = App.prefs().getBoolean(
							R.string.pref_displayDebugDetails, R.bool.pref_displayDebugDetails_default);

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

		@Override public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override public boolean isViewFromObject(View view, Object obj) {
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
