package net.twisterrob.inventory.android.fragment.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.*;
import android.support.annotation.RawRes;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.tasks.SaveToFile;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.TextWatcherAdapter;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public abstract class BaseEditFragment<T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);
	public static final String EDIT_IMAGE = "editImageOnStartup";

	/** full path */
	private String currentImage;
	private boolean keepNameInSync;

	protected EditText name;
	protected EditText description;
	protected Spinner type;
	protected CursorAdapter typeAdapter;
	protected ImageView image;
	private boolean isClean;

	public void setKeepNameInSync(boolean keepNameInSync) {
		this.keepNameInSync = keepNameInSync;
	}

	public boolean isKeepNameInSync() {
		return keepNameInSync;
	}

	public boolean isDirty() {
		return !isClean;
	}

	protected File getTargetFile() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		String imageFileName = getBaseFileName() + "_" + timeStamp + ".jpg";
		File storageDir = getContext().getCacheDir();
		return new File(storageDir, imageFileName);
	}

	protected abstract String getBaseFileName();

	protected void onSingleRowLoaded(ImagedDTO dto) {
		AndroidTools.selectByID(type, dto.type);
		name.setText(dto.name); // must set it after type to prevent keepNameInSync
		setCurrentImage(dto.getImage(getContext()));
		description.setText(dto.description);
		if (getArguments().getBoolean(EDIT_IMAGE)) {
			getArguments().remove(EDIT_IMAGE);
			takePicture();
		}
		new Handler(getActivity().getMainLooper()).post(new Runnable() {
			@Override public void run() {
				isClean = true;
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		image = (ImageView)view.findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				takePicture();
			}
		});
		image.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				removePicture();
				return true;
			}
		});

		name = (EditText)view.findViewById(R.id.title);
		name.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				isClean = false;
				doValidateTitle();
			}
		});

		description = (EditText)view.findViewById(R.id.description);
		description.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				isClean = false;
			}
		});

		((Button)view.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		type = (Spinner)view.findViewById(R.id.type);
		type.setAdapter(typeAdapter = new TypeAdapter(getContext()));
		type.setOnItemSelectedListener(new DefaultValueUpdater(name, CommonColumns.NAME) {
			private int oldPos = AdapterView.INVALID_POSITION;
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (oldPos != position) {
					oldPos = position;
					isClean = false;
				}
				if (isKeepNameInSync()) {
					super.onItemSelected(parent, view, position, id);
				}
				if (getCurrentImage() == null) {
					setCurrentImage(null);
				} // else leave current image as is
			}
		});
	}

	public void save() {
		doPrepareSave();
		if (doValidate()) {
			doSave();
		}
	}

	protected void doPrepareSave() {
		name.setText(name.getText().toString().trim());
	}

	protected abstract void doSave();

	protected boolean doValidate() {
		boolean valid = true;
		valid &= doValidateTitle();
		return valid;
	}

	protected boolean doValidateTitle() {
		if (TextUtils.getTrimmedLength(name.getText()) == 0) {
			name.setError("Please enter some text");
			return false;
		} else {
			name.setError(null);
			return true;
		}
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
		isClean = false;
		if (currentImage == null) {
			int typeImageID = getTypeImage(type.getSelectedItemPosition());
			Pic.SVG_REQUEST.load(typeImageID).into(image);
		} else {
			Pic.IMAGE_REQUEST.load(currentImage).into(image);
		}
	}
}
