package net.twisterrob.inventory.android.view;

import android.animation.*;
import android.annotation.SuppressLint;
import android.content.*;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.content.Loader;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class ChangeTypeDialog {
	private final @NonNull BaseFragment<?> fragment;
	private final @NonNull Context context;
	private ListView list;
	private @NonNull Drawable spinner;
	private @Nullable Animator spinnerAnim;

	public ChangeTypeDialog(@NonNull BaseFragment<?> fragment) {
		this.fragment = fragment;
		this.context = fragment.requireActivity();

		initSpinner();
	}

	private void initSpinner() {
		spinner = ContextCompat.getDrawable(context, R.drawable.spinner);
		spinner.setColorFilter(Constants.Pic.tint());
		if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
			spinnerAnim = ObjectAnimator.ofInt(spinner, "level", 0, 10000);
			((ValueAnimator)spinnerAnim).setRepeatCount(ObjectAnimator.INFINITE);
		}
	}

	private void startShowLoading() {
		if (spinnerAnim != null) {
			spinnerAnim.start();
		}
	}

	private void finishShowLoading() {
		if (spinnerAnim != null) {
			spinnerAnim.end();
		}
	}

	public void show(final Variants variants, long initialType) {
		final TypeAdapter adapter = new TypeAdapter(context);
		adapter.setExpandable(variants.isExpandable());
		adapter.setIndented(true);
		Builder builder = new Builder(context)
				.setIcon(spinner)
				.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
					@Override public void onClick(final DialogInterface dialog, int which) {
						StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
						try {
							// at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:153)
							Cursor cursor = (Cursor)adapter.getItem(which);
							@SuppressLint("Range")
							final long newType = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
							if (!DatabaseTools.getOptionalBoolean(cursor, CommonColumns.COUNT_CHILDREN_DIRECT, false)) {
								// no children, nothing to expand, auto-save now
								userSelected(cursor, variants);
								dialog.dismiss();
							} else {
								load(variants, adapter, newType);
							}
						} finally {
							StrictMode.setThreadPolicy(originalPolicy);
						}
					}
				})
				.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						int position = list.getCheckedItemPosition();
						if (position == AdapterView.INVALID_POSITION
								|| adapter.getCount() <= position) {
							throw new IllegalStateException(
									"Wrong item selected, this shouldn't happen: "
											+ position + " in list of " + adapter.getCount());
						}
						StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
						try {
							Cursor cursor = (Cursor)adapter.getItem(position);
							userSelected(cursor, variants);
						} finally {
							StrictMode.setThreadPolicy(originalPolicy);
						}
						dialog.dismiss();
					}
				});
		variants.augment(builder);
		AlertDialog dialog = builder.create();
		list = dialog.getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
				showKeywords(context, variants.getTypeName(cursor), variants.getKeywords(cursor));
				return true;
			}
		});
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		load(variants, adapter, initialType);
	}
	private void load(@NonNull Variants variants, @NonNull TypeAdapter adapter, long type) {
		Bundle args = variants.createArgs(type);
		MyCursorSwapper callback = new MyCursorSwapper(adapter, type);
		if (args == null) {
			fragment.getLoaderManager().initLoader(variants.getTypesLoader().id(), null, callback);
		} else {
			fragment.getLoaderManager().restartLoader(variants.getTypesLoader().id(), args, callback);
		}
	}

	private void userSelected(Cursor cursor, Variants variants) {
		variants.update(cursor);
	}

	private void autoSelect(final CursorAdapter adapter, final long type) {
		final int pos = findItemPosition(adapter, type);
		list.setItemChecked(pos, true);

		Cursor cursor = (Cursor)adapter.getItem(pos);
		int rootPos = findItemPosition(adapter, DatabaseTools.getOptionalInt(cursor, "root", Integer.MAX_VALUE));
		final int offset = rootPos < 0? Integer.MAX_VALUE : (1 + pos - rootPos) * getHeight(adapter, pos);
		// Delay things, so that ListView has time to lay out the new Adapter
		// if it was just set in the same frame as this call.
		list.post(new Runnable() {
			@Override public void run() {
				if (offset < list.getHeight()) {
					float scale = Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
					int duration = (int)(scale * 200);
					list.smoothScrollToPositionFromTop(pos, offset, duration);
				} else {
					list.smoothScrollToPosition(pos);
				}
			}
		});
	}

	private int getHeight(CursorAdapter adapter, int pos) {
		//int height = context.getResources().getDimensionPixelSize(R.dimen.icon_small)
		//		+ 2 * context.getResources().getDimensionPixelSize(R.dimen.margin);
		View view = adapter.getView(pos, null, list);
		int UNBOUNDED = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(UNBOUNDED, UNBOUNDED);
		return view.getMeasuredHeight() + list.getDividerHeight();
	}

	public static void showKeywords(Context context, CharSequence title, CharSequence keywords) {
		if (TextUtils.isEmpty(title)) {
			return;
		}
		if (TextUtils.isEmpty(keywords)) {
			keywords = context.getText(R.string.category_keywords_empty);
		}
		title = context.getString(R.string.category_keywords_of, title);
		AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(keywords)
				.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	public static abstract class Variants {
		@UiThread protected abstract void update(Cursor cursor);
		@UiThread protected abstract CharSequence getTitle();
		@UiThread protected abstract Loaders getTypesLoader();
		@UiThread protected abstract CharSequence getName();
		@UiThread protected abstract boolean isExpandable();
		@UiThread protected void augment(Builder dialog) {
			dialog.setTitle(getTitle());
		}
		@UiThread protected Bundle createArgs(long type) {
			return null;
		}
		@UiThread public abstract CharSequence getTypeName(Cursor cursor);
		@UiThread public abstract CharSequence getKeywords(Cursor cursor);
	}

	private class MyCursorSwapper extends CursorSwapper {
		private final long type;
		public MyCursorSwapper(@NonNull TypeAdapter adapter, long type) {
			super(ChangeTypeDialog.this.context, adapter);
			this.type = type;
			startShowLoading();
		}
		@Override public void onLoadFinished(@NonNull Loader<Cursor> loader, @Nullable Cursor data) {
			super.onLoadFinished(loader, data);
			if (type != -1) {
				autoSelect(adapter, type);
			}
		}
		@Override protected void updateAdapter(@Nullable Cursor data) {
			super.updateAdapter(data);
			finishShowLoading();
		}
	}
}
