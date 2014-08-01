package net.twisterrob.inventory.android.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.view.*;
import android.widget.ImageView;

import com.google.android.gms.drive.*;
import com.squareup.picasso.RequestCreator;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.CaptureImage;
import net.twisterrob.inventory.android.tasks.Upload;
import net.twisterrob.inventory.android.utils.*;

public abstract class BaseEditFragment<T> extends BaseFragment<T> {
	protected static final String DYN_ImageView = "imageViewID";
	private DriveId driveId;

	protected File getTargetFile() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		String imageFileName = getBaseFileName() + "_" + timeStamp + ".jpg";
		File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return new File(storageDir, imageFileName);
	}

	protected abstract String getBaseFileName();

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.picture, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_picture_take:
				startActivityForResult(CaptureImage.saveTo(getTargetFile()), PictureUtils.REQUEST_CODE_TAKE_PICTURE);
				return true;
			case R.id.action_picture_pick:
				startActivityForResult(PictureHelper.createGalleryIntent(), PictureUtils.REQUEST_CODE_GET_PICTURE);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PictureUtils.REQUEST_CODE_GET_PICTURE:
			case PictureUtils.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null) {
					File file = PictureUtils.getFile(getActivity(), data.getData());
					App.pic().load(data.getData()).into(getImageView());

					new Upload(getActivity()) {
						private final Logger LOG = LoggerFactory.getLogger(getClass());

						@Override
						protected void onPostExecute(DriveFile result) {
							if (result == null) {
								LOG.error("No driveId after upload");
								return;
							}
							setCurrentImageDriveId(result.getDriveId(), 0);
						}
					}.execute(file);
					return;
				}
				break;
			default:
				// do as super pleases
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private ImageView getImageView() {
		return (ImageView)getView().findViewById(super.<Integer> getDynamicResource(DYN_ImageView));
	}

	protected DriveId getCurrentImageDriveId() {
		return driveId;
	}
	protected void setCurrentImageDriveId(DriveId driveId, int fallbackResource) {
		this.driveId = driveId;
		RequestCreator load = App.pic().load(driveId);
		if (0 < fallbackResource) {
			load.placeholder(fallbackResource).error(fallbackResource);
		}
		load.into(getImageView());
	}
}
