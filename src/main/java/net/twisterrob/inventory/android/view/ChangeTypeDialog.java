package net.twisterrob.inventory.android.view;

import android.animation.*;
import android.annotation.TargetApi;
import android.content.*;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build.*;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class ChangeTypeDialog {
	private final BaseFragment fragment;
	private final Context context;
	private ListView list;
	private Drawable spinner;
	private Animator spinnerAnim;

	public ChangeTypeDialog(BaseFragment fragment) {
		this.fragment = fragment;
		this.context = fragment.getActivity();

		initSpinner();
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void initSpinner() {
		spinner = ContextCompat.getDrawable(context, R.drawable.spinner);
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			spinnerAnim = ObjectAnimator.ofInt(spinner, "level", 0, 10000);
			((ValueAnimator)spinnerAnim).setRepeatCount(ObjectAnimator.INFINITE);
		}
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void startShowLoading() {
		if (spinnerAnim != null) {
			spinnerAnim.start();
		}
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void finishShowLoading() {
		if (spinnerAnim != null) {
			spinnerAnim.end();
		}
	}

	public void show(final Variants variants, long initialType) {
		final TypeAdapter adapter = new TypeAdapter(context);
		Builder builder = new Builder(context)
				.setIcon(spinner)
				.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
					@Override public void onClick(final DialogInterface dialog, int which) {
						Cursor cursor = (Cursor)adapter.getItem(which);
						final long newType = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
						if (!DatabaseTools.getOptionalBoolean(cursor, CommonColumns.COUNT_CHILDREN_DIRECT, false)) {
							// no children, nothing to expand, auto-save now
							userSelected(cursor, variants);
							dialog.dismiss();
						} else {
							load(variants, adapter, newType);
						}
					}
				})
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						Cursor cursor = (Cursor)adapter.getItem(list.getCheckedItemPosition());
						userSelected(cursor, variants);
						dialog.dismiss();
					}
				});
		variants.augment(builder);
		AlertDialog dialog = builder.create();
		list = dialog.getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
				showKeywords(parent.getContext(), DatabaseTools.getString(cursor, Category.NAME));
				return true;
			}
		});
		dialog.show();
		load(variants, adapter, initialType);
	}
	private void load(Variants variants, TypeAdapter adapter, long type) {
		Bundle args = variants.createArgs(type);
		MyCursorSwapper callback = new MyCursorSwapper(adapter, type);
		if (args == null) {
			fragment.getLoaderManager().initLoader(variants.getTypesLoader().id(), null, callback);
		} else {
			fragment.getLoaderManager().restartLoader(variants.getTypesLoader().id(), args, callback);
		}
	}

	private void userSelected(Cursor cursor, Variants variants) {
		long newType = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
		variants.update(newType, cursor); // FIXME DB on UI
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void autoSelect(final CursorAdapter adapter, final long type) {
		final int pos = findItemPosition(adapter, type);
		list.setItemChecked(pos, true);

		Cursor cursor = (Cursor)adapter.getItem(pos);
		int rootPos = findItemPosition(adapter, DatabaseTools.getOptionalInt(cursor, "root", Integer.MAX_VALUE));
		final int offset = rootPos < 0? Integer.MAX_VALUE : (1 + pos - rootPos) * getHeight(adapter, pos);
		list.post(new Runnable() {
			@Override public void run() {
				if (offset < list.getHeight() && VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
					list.smoothScrollToPositionFromTop(pos, offset);
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

	public static void showKeywords(Context context, String categoryName) {
		CharSequence categoryTitle = getText(context, categoryName);
		CharSequence categoryKeywords = CategoryDTO.getKeywords(context, categoryName, true);
		new android.app.AlertDialog.Builder(context)
				.setTitle(context.getString(R.string.category_keywords, categoryTitle))
				.setMessage(categoryKeywords)
				.show()
		;
	}

	public static abstract class Variants {
		protected abstract void update(long newType, Cursor cursor);
		protected abstract CharSequence getTitle();
		protected abstract Loaders getTypesLoader();
		protected abstract CharSequence getName();
		protected void augment(Builder dialog) {
			dialog.setTitle(getTitle());
		}
		protected Bundle createArgs(long type) {
			return null;
		}
	}

	private class MyCursorSwapper extends CursorSwapper {
		private final long type;
		public MyCursorSwapper(TypeAdapter adapter, long type) {
			super(ChangeTypeDialog.this.context, adapter);
			this.type = type;
			startShowLoading();
		}
		@Override public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
			super.onLoadFinished(loader, data);
			if (type != -1) {
				autoSelect(adapter, type);
			}
		}
		@Override protected void updateAdapter(Cursor data) {
			super.updateAdapter(data);
			finishShowLoading();
		}
	}
}
