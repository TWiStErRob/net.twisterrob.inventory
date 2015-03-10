package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.*;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import static android.content.Context.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.view.DetailsAdapter.ViewHolder;

public class DetailsAdapter extends CursorRecyclerAdapter<ViewHolder> {
	private static final Logger LOG = LoggerFactory.getLogger(DetailsAdapter.class);

	public interface DetailsEvent {
		void showImage(String path);
		void editImage(long id);
	}

	private final DetailsEvent listener;

	public DetailsAdapter(DetailsEvent listener) {
		super(null);
		this.listener = listener;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
			image = (ImageView)view.findViewById(R.id.image);
			description = (TextView)view.findViewById(R.id.description);
			pager = (ViewPager)view.findViewById(R.id.pager);
			pager.setCurrentItem(getDefaultPageIndex(itemView.getContext()));
		}

		ViewPager pager;
		ImageView image;
		TextView description;
	}

	@Override public int getItemViewType(int position) {
		return R.layout.fragment_details;
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		if (holder.pager.getAdapter() == null) {
			CategoryDTO entity = CategoryDTO.fromCursor(cursor);
			holder.pager.setAdapter(new ImageAndDescriptionAdapter(holder.itemView.getContext(), entity));
		}
	}

	private static int getDefaultPageIndex(Context context) {
		String key = context.getString(R.string.pref_defaultEntityDetailsPage);
		String defaultValue = context.getString(R.string.pref_defaultEntityDetailsPage_default);
		String defaultPage = App.getPrefs().getString(key, defaultValue);
		return AndroidTools.findIndexInResourceArray(context,
				R.array.pref_defaultEntityDetailsPage_values, defaultPage);
	}

	private class ImageAndDescriptionAdapter extends PagerAdapter {
		private final Context context;
		private final ImagedDTO entity;

		public ImageAndDescriptionAdapter(Context context, ImagedDTO imageData) {
			this.context = context;
			this.entity = imageData;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return context.getResources().getTextArray(R.array.pref_defaultEntityDetailsPage_entries)[position];
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
					image.setOnClickListener(new OnClickListener() {
						@Override public void onClick(View v) {
							String path = entity.getImage(v.getContext());
							if (path != null) {
								listener.showImage(path);
							} else {
								listener.editImage(entity.id);
							}
						}
					});
					image.setOnLongClickListener(new OnLongClickListener() {
						@Override public boolean onLongClick(View v) {
							listener.editImage(entity.id);
							return true;
						}
					});

					int fallbackID = entity.getFallbackResource(image.getContext());
					String imagePath = entity.getImage(image.getContext());
					if (imagePath == null) {
						type.setImageDrawable(null);
						Pic.SVG_REQUEST.load(fallbackID).into(image);
					} else {
						Pic.SVG_REQUEST.load(fallbackID).into(type);
						Pic.IMAGE_REQUEST.load(imagePath).into(image);
					}
					break;
				}
				case 1: {
					view = inflater.inflate(R.layout.inc_details_details, container, false);
					TextView details = (TextView)view.findViewById(R.id.details);
					details.setText(entity.name);
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
	}
}
