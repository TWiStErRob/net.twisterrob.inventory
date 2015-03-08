package net.twisterrob.inventory.android.fragment.data;

import java.io.File;

import org.slf4j.*;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.activity.ImageActivity;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;

public abstract class BaseViewFragment<DTO extends ImagedDTO, T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseViewFragment.class);

	protected ImageView image;
	protected ViewPager pager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_details, container, false);
		image = (ImageView)root.findViewById(R.id.image);
		pager = (ViewPager)root.findViewById(R.id.pager);
		return root;
	}

	public void onSingleRowLoaded(DTO entity) {
		getBaseActivity().setActionBarTitle(entity.name);
		// FIXME getBaseActivity().setIcon(entity.getFallbackDrawable(getContext(),
		//		getActionbarIconSize(getContext()), getActionbarIconPadding(getContext())));
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
					image.setOnClickListener(new ImageOpenListener());
					image.setOnLongClickListener(new ImageChangeListener());

					int fallbackID = entity.getFallbackResource(image.getContext());
					String imagePath = entity.getImage(image.getContext());
					if (imagePath == null) {
						Pic.SVG_REQUEST.load(fallbackID).into(image);
					} else {
						Pic.IMAGE_REQUEST.load(imagePath).into(image);
					}
					break;
				}
				case 1: {
					view = inflater.inflate(R.layout.inc_details_details, container, false);
					TextView details = (TextView)view.findViewById(R.id.details);
					details.setText(getDetailsString(entity));
					//details.setMovementMethod(ScrollingMovementMethod.getInstance());
					details.setOnTouchListener(new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							// http://stackoverflow.com/questions/8121491/is-it-possible-to-add-a-scrollable-textview-to-a-listview
							container.getParent().requestDisallowInterceptTouchEvent(true);
							return false;
						}
					});
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
				try {
					String path = entity.getImage(getContext());
					if (path != null) {
						showImage(path);
					} else {
						editImage();
					}
				} catch (Exception ex) {
					LOG.warn("Cannot start image viewer for {}", entity, ex);
				}
			}

			private void showImage(String path) {
				File file = new File(path);
				Uri uri = FileProvider.getUriForFile(getContext(), Constants.AUTHORITY_IMAGES, file);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				if (App.getPrefs().getBoolean(getString(R.string.pref_internalImageViewer), true)) {
					intent.setComponent(new ComponentName(getContext(), ImageActivity.class));
				}
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(uri, "image/jpeg");
				getActivity().startActivity(intent);
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
