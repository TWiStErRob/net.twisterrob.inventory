package net.twisterrob.inventory.android.fragment.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.tasks.SaveToFile;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.TypeAdapter;

public abstract class BaseEditFragment<T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);

	/** full path */
	private String currentImage;
	private boolean keepNameInSync;

	protected EditText title;
	protected Spinner type;
	protected CursorAdapter typeAdapter;
	protected ImageView image;

	public void setKeepNameInSync(boolean keepNameInSync) {
		this.keepNameInSync = keepNameInSync;
	}

	public boolean isKeepNameInSync() {
		return keepNameInSync;
	}

	protected File getTargetFile() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		String imageFileName = getBaseFileName() + "_" + timeStamp + ".jpg";
		File storageDir = getContext().getCacheDir();
		return new File(storageDir, imageFileName);
	}

	protected abstract String getBaseFileName();

	protected abstract void save();

	protected void onSingleRowLoaded(ImagedDTO dto, long typeID) {
		AndroidTools.selectByID(type, typeID); // sets icon
		getBaseActivity().setActionBarTitle(dto.name);
		title.setText(dto.name); // must set it after type to prevent keepNameInSync
		setCurrentImage(dto.getImage(getContext()));
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		title = (EditText)view.findViewById(R.id.title);
		type = (Spinner)view.findViewById(R.id.type);
		image = (ImageView)view.findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				takePicture();
			}
		});

		((Button)view.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		type.setAdapter(typeAdapter = new TypeAdapter(getContext()));
		type.setOnItemSelectedListener(new DefaultValueUpdater(title, CommonColumns.NAME) {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (isKeepNameInSync()) {
					super.onItemSelected(parent, view, position, id);
				}
				if (getCurrentImage() == null) {
					setCurrentImage(null);
				} // else leave current image as is
				getBaseActivity().setIcon(getTypeImage(position));
			}
		});
	}

	private @RawRes int getTypeImage(int position) {
		@SuppressWarnings("resource")
		Cursor cursor = (Cursor)type.getItemAtPosition(position);
		return ImagedDTO.getFallbackID(getContext(), cursor);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.picture, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_picture_take:
				takePicture();
				return true;
			case R.id.action_picture_remove:
				removePicture();
				return true;
			case R.id.action_picture_pick:
				pickPicture();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void pickPicture() {
		Intent intent = PictureHelper.createGalleryIntent();
		startActivityForResult(intent, ImageTools.REQUEST_CODE_GET_PICTURE);
	}

	private void takePicture() {
		Intent intent = CaptureImage.saveTo(getContext(), getTargetFile());
		startActivityForResult(intent, ImageTools.REQUEST_CODE_TAKE_PICTURE);
	}

	private void removePicture() {
		setCurrentImage(null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ImageTools.REQUEST_CODE_GET_PICTURE:
			case ImageTools.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null) {
					image.setImageResource(R.drawable.image_loading);
					try {
						File file = ImageTools.getFile(getContext(), data.getData());
						new SaveToFile(getContext()) {
							@Override
							protected void onPostExecute(File result) {
								if (result != null) {
									setCurrentImage(result.getAbsolutePath());
								}
							}
						}.execute(file);
					} catch (RuntimeException ex) {
						image.setImageResource(R.drawable.image_error);
						throw ex;
					}
					return;
				}
				break;
			default:
				// do as super pleases
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected String getCurrentImage() {
		return currentImage;
	}

	protected void setCurrentImage(String currentImage) {
		this.currentImage = currentImage;
		Drawable fallback = App.pic().getSVG(getContext(), getTypeImage(type.getSelectedItemPosition()));
		if (currentImage == null) {
			image.setImageDrawable(fallback);
		} else {
			App.pic().start(this).placeholder(fallback).load(currentImage).into(image);
		}
	}
}
