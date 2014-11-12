package net.twisterrob.inventory.android.fragment.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.google.android.gms.drive.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.utils.tools.ImageTools;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.tasks.Upload;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.TypeAdapter;

public abstract class BaseEditFragment<T> extends BaseSingleLoaderFragment<T> {
	private DriveId driveId;
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
		File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return new File(storageDir, imageFileName);
	}

	protected abstract String getBaseFileName();

	protected abstract void save();

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

		type.setAdapter(typeAdapter = new TypeAdapter(getActivity()));
		type.setOnItemSelectedListener(new DefaultValueUpdater(title, CommonColumns.NAME) {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (keepNameInSync) {
					super.onItemSelected(parent, view, position, id);
				}
				if (getCurrentImageDriveId() == null) {
					@SuppressWarnings("resource")
					Cursor cursor = (Cursor)parent.getItemAtPosition(position);
					String image = cursor.getString(cursor.getColumnIndex(CommonColumns.TYPE_IMAGE));
					setCurrentImageDriveId(null, ImagedDTO.getFallbackDrawable(getActivity(), image));
				}
			}
		});
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
		Intent intent = CaptureImage.saveTo(getActivity(), getTargetFile());
		startActivityForResult(intent, ImageTools.REQUEST_CODE_TAKE_PICTURE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ImageTools.REQUEST_CODE_GET_PICTURE:
			case ImageTools.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null) {
					image.setImageResource(R.drawable.image_loading);
					try {
						File file = ImageTools.getFile(getActivity(), data.getData());
						new Upload(getActivity()) {
							@Override
							protected void onPostExecute(DriveFile result) {
								if (result != null) {
									setCurrentImageDriveId(result.getDriveId(), null);
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

	protected DriveId getCurrentImageDriveId() {
		return driveId;
	}

	protected void setCurrentImageDriveId(DriveId driveId, int fallbackResourceID) {
		setCurrentImageDriveId(driveId, getResources().getDrawable(fallbackResourceID));
	}

	protected void setCurrentImageDriveId(DriveId driveId, Drawable fallback) {
		this.driveId = driveId;
		App.pic().loadDrive(this, driveId).placeholder(fallback).into(image);
	}
}
