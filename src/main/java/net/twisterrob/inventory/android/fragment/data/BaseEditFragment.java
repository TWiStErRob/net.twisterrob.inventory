package net.twisterrob.inventory.android.fragment.data;

import java.io.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.content.glide.LongSignature;
import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.TextWatcherAdapter;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public abstract class BaseEditFragment<T> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);
	public static final String EDIT_IMAGE = "editImageOnStartup";

	private Uri currentImage;
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

	protected void onSingleRowLoaded(ImagedDTO dto) {
		AndroidTools.selectByID(type, dto.type);
		name.setText(dto.name); // must set it after type to prevent keepNameInSync
		setCurrentImage(dto.image? dto.getImageUri() : null);
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
				reloadImage();
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
		new SimpleSafeAsyncTask<Context, Void, File>() {
			@Override protected @Nullable File doInBackground(@Nullable Context context) throws IOException {
				return Constants.Paths.getTempImage(context);
			}
			@Override protected void onResult(@Nullable File file, Context context) {
				try {
					Intent intent = CaptureImage.saveTo(getContext(), file);
					startActivityForResult(intent, ImageTools.REQUEST_CODE_TAKE_PICTURE);
				} catch (RuntimeException ex) {
					onError(ex, context);
				}
			}
			@Override protected void onError(@NonNull Exception ex, Context context) {
				LOG.error("Cannot take picture", ex);
				App.toastUser("Cannot take picture: " + ex);
			}
		}.execute(getContext());
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
						setCurrentImage(data.getData());
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

	protected Uri getCurrentImage() {
		return currentImage;
	}

	protected void setCurrentImage(Uri currentImage) {
		this.currentImage = currentImage;
		isClean = false;
		reloadImage();
	}

	private void reloadImage() {
		if (currentImage == null) {
			int typeImageID = getTypeImage(type.getSelectedItemPosition());
			Pic.SVG_REQUEST.load(typeImageID).into(image);
		} else {
			Pic.IMAGE_REQUEST
					.signature(new LongSignature(System.currentTimeMillis()))
					.load(currentImage.toString())
					.into(image);
		}
	}

	protected static abstract class BaseSaveTask<T extends ImagedDTO> extends SimpleSafeAsyncTask<T, Void, T> {
		@Override protected final T doInBackground(T param) throws Exception {
			Database db = App.db().beginTransaction();
			try {
				param = saveInTransaction(db, param);
				db.setTransactionSuccessful();
				return param;
			} finally {
				db.endTransaction();
			}
		}
		protected abstract T saveInTransaction(Database db, T param) throws Exception;

		@Override protected void onError(@NonNull Exception ex, T param) {
			LOG.warn("Cannot save ({}){}", param != null? param.getClass().getSimpleName() : null, param, ex);
			App.toastUser("Name must be unique within the collection");
		}
	}
}
