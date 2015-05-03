package net.twisterrob.inventory.android.fragment.data;

import java.io.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.TextWatcherAdapter;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public abstract class BaseEditFragment<T, DTO extends ImagedDTO> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);
	public static final String EDIT_IMAGE = "editImageOnStartup";

	/** byte[], Uri or null */
	private Object currentImage;
	private boolean keepNameInSync;

	protected EditText name;
	protected EditText description;
	private Spinner type;
	protected CursorAdapter typeAdapter;
	private ImageView image;
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

	protected void onSingleRowLoaded(DTO dto) {
		AndroidTools.selectByID(type, dto.type);
		name.setText(dto.name); // must set it after type to prevent keepNameInSync
		setCurrentImage(dto.hasImage? dto.getImageUri() : null);
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

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	// TODO maybe move overriding logic into this class
	@Override public void onViewCreated(View view, Bundle bundle) {
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

	@SuppressWarnings("unchecked")
	protected void doSave() {
		DTO dto = createDTO();
		dto.hasImage = currentImage != null;
		dto.image = currentImage instanceof byte[]? (byte[])currentImage : null;
		dto.name = name.getText().toString();
		dto.description = description.getText().toString();
		dto.type = type.getSelectedItemId();
		new SaveTask().execute(dto);
	}
	/** Create the DTO object with id set and fill in all fields needed to save (except the ones inherited from ImagedDTO) */
	protected abstract DTO createDTO();
	protected abstract DTO onSave(Database db, DTO param) throws Exception;
	protected abstract void onSaved(DTO result);

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

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.picture, menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
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
				App.toastUser(App.getError(ex, "Cannot take picture."));
			}
		}.execute(getContext());
	}

	private void removePicture() {
		setCurrentImage(null);
	}

	@Override public void onActivityResult(final int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ImageTools.REQUEST_CODE_GET_PICTURE:
			case ImageTools.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
					handleResult(requestCode, data.getData());
				}
				return;
			default:
				// do as super pleases
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setCurrentImage(Object currentImage) {
		this.currentImage = currentImage;
		isClean = false;
		reloadImage();
	}

	private void reloadImage() {
		if (currentImage == null) {
			int typeImageID = getTypeImage(type.getSelectedItemPosition());
			Pic.svg().load(typeImageID).into(image);
		} else if (currentImage instanceof Uri) {
			Pic.jpg()
					.signature(new LongSignature(System.currentTimeMillis())) // TODO =image_time but from where?
					.load(currentImage.toString())
					.into(image);
		} else if (currentImage instanceof byte[]) {
			Pic.baseRequest(byte[].class) // no need for signature, the byte[] doesn't change -> TODO glide#437
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(true)
					.signature(new LongSignature(System.currentTimeMillis()))
					.listener(new LoggingListener<byte[], GlideDrawable>("edit"))
					.load((byte[])currentImage)
					.into(image);
		} else {
			throw new IllegalStateException("Unrecognized image: " + currentImage);
		}
	}

	private void handleResult(final int requestCode, final Uri uri) {
		Glide
				.with(this)
				.load(uri)
				.asBitmap()
				.toBytes(CompressFormat.JPEG, 80) // XXX good enough?
				.override(500, 500)
				.fitCenter()
				.into(new SimpleTarget<byte[]>() {
					@Override public void onLoadStarted(Drawable placeholder) {
						int typeImageID = getTypeImage(type.getSelectedItemPosition());
						Pic.svg().load(typeImageID).into(image);
					}
					@Override public void onResourceReady(byte[] resource,
							GlideAnimation<? super byte[]> glideAnimation) {
						if (requestCode == ImageTools.REQUEST_CODE_TAKE_PICTURE
								&& ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
							//noinspection ResultOfMethodCallIgnored: best effort, can't guarantee anything
							new File(uri.getPath()).delete();
						}
						setCurrentImage(resource);
					}
					@Override public void onLoadFailed(Exception e, Drawable errorDrawable) {
						App.toastUser(App.getError(e, "Cannot process image: " + uri));
					}
				})
		;
	}

	protected class SaveTask extends SimpleSafeAsyncTask<DTO, Void, DTO> {
		@Override protected final DTO doInBackground(DTO param) throws Exception {
			Database db = App.db().beginTransaction();
			try {
				param = onSave(db, param);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			return param;
		}

		@Override protected void onResult(DTO result, DTO param) {
			onSaved(result);
		}

		@Override protected void onError(@NonNull Exception ex, DTO param) {
			LOG.warn("Cannot save ({}){}", param != null? param.getClass().getSimpleName() : null, param, ex);
			App.toastUser(App.getError(ex, R.string.generic_error_save));
		}
	}
}
