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
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.DetailsAdapter.ViewHolder;

public class DetailsAdapter extends CursorRecyclerAdapter<ViewHolder> {
	private static final Logger LOG = LoggerFactory.getLogger(DetailsAdapter.class);

	public interface DetailsEvents {
		void showImage(String path);
		void editImage(long id);
	}

	private final DetailsEvents listener;

	public DetailsAdapter(DetailsEvents listener) {
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
		return R.layout.item_details;
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		if (holder.pager.getAdapter() == null) {
			Context context = holder.itemView.getContext();
			holder.pager.setAdapter(new ImageAndDescriptionAdapter(context,
					holder.getItemId(),
					getDetailsString(context, cursor),
					getImage(context, cursor),
					getTypeImage(context, cursor)));
		}
	}

	private String getImage(Context context, Cursor cursor) {
		int index = cursor.getColumnIndex(CommonColumns.IMAGE);
		if (index != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.getString(index);
		} else {
			return null;
		}
	}

	private int getTypeImage(Context context, Cursor cursor) {
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(context, typeImage);
	}

	protected CharSequence getDetailsString(Context context, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(context, name);
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
		private final long id;
		private final CharSequence description;
		private final String imagePath;
		private final int fallbackID;

		public ImageAndDescriptionAdapter(Context context, long id, CharSequence description, String imagePath,
				int fallbackID) {

			this.context = context;
			this.id = id;
			this.description = description;
			this.imagePath = imagePath;
			this.fallbackID = fallbackID;
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
							if (imagePath != null) {
								listener.showImage(imagePath);
							} else {
								listener.editImage(id);
							}
						}
					});
					image.setOnLongClickListener(new OnLongClickListener() {
						@Override public boolean onLongClick(View v) {
							listener.editImage(id);
							return true;
						}
					});

					ImagedDTO.loadInto(image, type, imagePath, fallbackID, false);
					break;
				}
				case 1: {
					view = inflater.inflate(R.layout.inc_details_details, container, false);
					TextView details = (TextView)view.findViewById(R.id.details);
					details.setText(description);
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
