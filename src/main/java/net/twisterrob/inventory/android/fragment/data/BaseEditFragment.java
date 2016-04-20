package net.twisterrob.inventory.android.fragment.data;

import java.io.File;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.*;
import android.text.TextUtils;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.*;
import android.widget.*;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.bumptech.glide.DrawableRequestBuilder;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.content.glide.LongSignature;
import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.TextWatcherAdapter;
import net.twisterrob.android.wiring.DefaultValueUpdater;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.content.model.helpers.Hinter;
import net.twisterrob.inventory.android.content.model.helpers.Hinter.CategorySelectedEvent;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.utils.NestedScrollableRecyclerViewListener;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.ChangeTypeDialog.Variants;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public abstract class BaseEditFragment<T, DTO extends ImagedDTO> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);
	public static final String EDIT_IMAGE = "editImageOnStartup";
	protected static final String DYN_NameHintResource = "nameHint";
	protected static final String DYN_DescriptionHintResource = "descriptionHint";

	private boolean isRestored;
	private Uri restoredImage;
	private int restoredTypePos;

	private Uri currentImage;
	private boolean keepNameInSync;
	private DTO original;

	private EditText name;
	private EditText description;
	private Spinner type;
	private RecyclerView hint;
	private Hinter hinter;
	private CursorAdapter typeAdapter;
	private ImageView image;
	private ImageView typeImage;
	private boolean isClean = true;

	protected void setKeepNameInSync(boolean keepNameInSync) {
		this.keepNameInSync = keepNameInSync;
	}

	public boolean isDirty() {
		return !isClean;
	}

	protected LoaderCallbacks<Cursor> getTypeCallback() {
		return new CursorSwapper(getContext(), typeAdapter) {
			@Override protected void updateAdapter(Cursor data) {
				super.updateAdapter(data);
				if (isNew()) {
					tryRestore();
				}
			}
		};
	}

	protected void onSingleRowLoaded(final DTO dto) {
		original = dto;
		if (tryRestore()) {
			return;
		}

		AndroidTools.selectByID(type, dto.type);
		name.setText(dto.name); // must set it after type to prevent keepNameInSync
		resetPicture(); // displays image, so needs type to be selected
		description.setText(dto.description);
		if (getArguments().getBoolean(EDIT_IMAGE)) {
			getArguments().remove(EDIT_IMAGE);
			getPicture();
		}
		type.post(new Runnable() {
			@Override public void run() {
				isClean = true;
			}
		});
		if (this instanceof ItemEditFragment) {
			typeImage.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					new ChangeTypeDialog(BaseEditFragment.this).show(new Variants() {
						@Override protected void update(Cursor cursor) {
							AndroidTools.selectByID(type, DatabaseTools.getLong(cursor, Item.ID));
						}
						@Override protected CharSequence getTitle() {
							return "Change Category of " + getName();
						}
						@Override protected Loaders getTypesLoader() {
							return Loaders.ItemCategories;
						}
						@Override protected CharSequence getName() {
							return name.getText();
						}
						@Override protected boolean isExpandable() {
							return true; // only for items (see instanceof above)
						}
						@Override protected Bundle createArgs(long type) {
							return Intents.bundleFromCategory(type);
						}
					}, type.getSelectedItemId());
				}
			});
		}
	}

	private boolean tryRestore() {
		if (isRestored) {
			if (currentImage == null) {
				// only restore if there's not an image currently set
				// (onActivityResult called before the loader finishes)
				setCurrentImage(restoredImage);
			}
			// manually restore because sometimes lost, e.g. action_take_picture, rotate, back (or crop)
			// at this point we can be sure that types are loaded because SingleBelonging loader depends on types loader
			type.setSelection(restoredTypePos);
			// isClean cannot be restored even if post()-ed here, setSelection finishes after
			return true;
		}
		return false;
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("image", currentImage);
		outState.putInt("typePos", type.getSelectedItemPosition());
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			restoredImage = savedInstanceState.getParcelable("image");
			restoredTypePos = savedInstanceState.getInt("typePos");
			isRestored = true;
		}
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	// CONSIDER moving overriding logic into this class
	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		image = (ImageView)view.findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getPicture();
			}
		});
		image.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				removePicture();
				return true;
			}
		});

		typeImage = (ImageView)view.findViewById(R.id.type);
		AndroidTools.displayedIf(typeImage, this instanceof ItemEditFragment);

		name = (EditText)view.findViewById(R.id.title);
		AndroidTools.setHint(name, (Integer)getDynamicResource(DYN_NameHintResource));
		name.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				isClean = false;
				if (doValidateTitle()) {
					updateHint(s, false);
				}
			}
		});

		description = (EditText)view.findViewById(R.id.description);
		AndroidTools.setHint(description, (Integer)getDynamicResource(DYN_DescriptionHintResource));
		description.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				isClean = false;
			}
		});

		//noinspection RedundantCast verify that btn_save is indeed a button 
		((Button)view.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		hint = (RecyclerView)view.findViewById(android.R.id.hint);
		hint.setLayoutManager(new LinearLayoutManager(getContext()));
		hint.addOnItemTouchListener(new NestedScrollableRecyclerViewListener(hint));
		hinter = new Hinter(getContext(), new CategorySelectedEvent() {
			@Override public void categorySelected(long categoryID) {
				AndroidTools.selectByID(type, categoryID);
				Hinter.unhighlight(name.getText());
			}
			@Override public void categoryQueried(long categoryID) {
				ChangeTypeDialog.showKeywords(getContext(), categoryID);
			}
		});
		hint.setAdapter(hinter.getAdapter());

		final ImageButton help = (ImageButton)view.findViewById(R.id.help);
		help.setOnClickListener(new OnClickListener() {
			@TargetApi(VERSION_CODES.HONEYCOMB)
			@Override public void onClick(View v) {
				if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
					getActivity().openContextMenu(help);
				} else {
					PopupMenu popup = new PopupMenu(v.getContext(), v);
					onPrepareContextMenu(popup.getMenu(), popup.getMenuInflater());
					popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override public boolean onMenuItemClick(MenuItem item) {
							return onContextItemSelected(item);
						}
					});
					popup.show();
				}
			}
		});
		AndroidTools.displayedIf(help, this instanceof ItemEditFragment);
		registerForContextMenu(help);

		type = (Spinner)view.findViewById(R.id.type_edit);
		type.setAdapter(typeAdapter = new TypeAdapter(getContext()));
		type.setOnItemSelectedListener(new DefaultValueUpdater(name, CommonColumns.NAME) {
			private int oldPos = AdapterView.INVALID_POSITION;
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (oldPos != position && oldPos != AdapterView.INVALID_POSITION) {
					isClean = false;
				}
				oldPos = position;
				if (keepNameInSync) {
					boolean cleanBefore = isClean; // automatic naming shouldn't be considered a change
					super.onItemSelected(parent, view, position, id);
					isClean = cleanBefore;
				}
				reloadImage();
				updateHint(name.getText(), false);
			}
		});
		// CONSIDER setOnItemLongClickListener is not supported, any way to work around? So user has the same "tooltip" as in ChangeTypeDialog
		type.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View view) {
				ChangeTypeDialog.showKeywords(view.getContext(), getTypeId());
				return true;
			}
		});
	}

	private void updateHint(CharSequence text, boolean b) {
		if (!(BaseEditFragment.this instanceof ItemEditFragment)) {
			return;
		}
		Hinter.unhighlight(name.getText());
		if (hinter.hint(text.toString(), b, getTypeName())) {
			AndroidTools.displayedIf(hint, true);
			hinter.highlight(name.getText());
		} else {
			AndroidTools.displayedIf(hint, false);
		}
	}

	@Override public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		onPrepareContextMenu(menu, getActivity().getMenuInflater());
	}

	private void onPrepareContextMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.edit_context, menu);
		String suggest = App.getSPref(R.string.pref_suggestCategory, R.string.pref_suggestCategory_default);
		String always = getString(R.string.pref_suggestCategory_always);
		menu.findItem(R.id.action_category_suggest).setVisible(!always.equals(suggest));
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_goto:
				startActivity(CategoryActivity.show(type.getSelectedItemId()));
				return true;
			case R.id.action_category_help:
				Intent intent = MainActivity.list(MainActivity.PAGE_CATEGORY_HELP);
				intent.putExtras(Intents.bundleFromCategory(getTypeId()));
				startActivity(intent);
				return true;
			case R.id.action_category_suggest:
				updateHint(name.getText(), true);
				return true;
			case R.id.action_category_keywords:
				ChangeTypeDialog.showKeywords(getContext(), getTypeId());
				return true;
		}
		return super.onContextItemSelected(item);
	}

	protected abstract boolean isNew();

	public void save() {
		doPrepareSave();
		if (doValidate()) {
			doSave();
		}
	}

	protected void doPrepareSave() {
		name.setText(name.getText().toString().trim());
	}

	protected void doSave() {
		DTO dto = createDTO();
		dto.hasImage = false; // will be set later on a background thread see SaveTask
		dto.image = null; // will be set later on a background thread see SaveTask
		dto.name = name.getText().toString();
		dto.description = description.getText().toString();
		dto.type = type.getSelectedItemId();
		AndroidTools.executePreferSerial(new SaveTask(), dto);
	}
	/** Create the DTO object with id set and fill in all fields needed to save (except the ones inherited from ImagedDTO) */
	protected abstract DTO createDTO();
	@SuppressWarnings("RedundantThrows")
	protected abstract DTO onSave(Database db, DTO param) throws Exception;
	protected abstract void onSaved(DTO result);

	private boolean doValidate() {
		return doValidateTitle();
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

	private long getTypeId() {
		Cursor cursor = (Cursor)type.getItemAtPosition(type.getSelectedItemPosition());
		return cursor != null? DatabaseTools.getLong(cursor, CommonColumns.ID) : CommonColumns.ID_ADD;
	}
	private String getTypeName() {
		Cursor cursor = (Cursor)type.getItemAtPosition(type.getSelectedItemPosition());
		return cursor != null? DatabaseTools.getString(cursor, CommonColumns.NAME) : null;
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.picture, menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_picture_get:
				getPicture();
				return true;
			case R.id.action_picture_reset:
				resetPicture();
				return true;
			case R.id.action_picture_remove:
				removePicture();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void resetPicture() {
		if (original != null && original.hasImage) {
			setCurrentImage(original.getImageUri());
		} else {
			setCurrentImage(null);
		}
	}

	private void removePicture() {
		setCurrentImage(null);
	}

	private void getPicture() {
		try {
			File file = Constants.Paths.getTempImage(getContext());
			Intent intent = CaptureImage.saveTo(getContext(), file, 2048/*px*/);
			intent.putExtra(CaptureImage.EXTRA_FORMAT, CompressFormat.JPEG);
			intent.putExtra(CaptureImage.EXTRA_QUALITY, 85/*%*/);
			startActivityForResult(intent, ImageTools.REQUEST_CODE_GET_PICTURE);
		} catch (Exception ex) {
			LOG.error("Cannot get picture", ex);
			App.toastUser(App.getError(ex, "Cannot get picture."));
		}
	}

	@Override public void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		switch (requestCode) {
			case ImageTools.REQUEST_CODE_GET_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
					setCurrentImage(data.getData());
				}
				return;
			default:
				// do as super pleases
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setCurrentImage(Uri currentImage) {
		this.currentImage = currentImage;
		isClean = false;
		reloadImage();
	}

	private void reloadImage() {
		if (currentImage == null) {
			loadTypeImage(image);
		} else {
			DrawableRequestBuilder<Uri> jpg = Pic.jpg();
			if (original != null && currentImage.equals(original.getImageUri())) {
				// original image needs timestamp to refresh between edits (in case user made save changes)
				jpg.signature(new LongSignature(original.imageTime));
			} else {
				// temporary image should be reloaded every time from the disk
				jpg.skipMemoryCache(true);
			}
			jpg.load(currentImage)
			   .diskCacheStrategy(NONE) // don't save any version: it's already on disk or used only once
			   .into(image);
		}
		loadTypeImage(typeImage);
	}

	private void loadTypeImage(ImageView target) {
		Cursor cursor = (Cursor)type.getItemAtPosition(type.getSelectedItemPosition());
		int typeImageID = cursor != null? ImagedDTO.getFallbackID(getContext(), cursor) : R.raw.category_unknown;
		Pic.svg().load(typeImageID).into(target);
	}

	protected class SaveTask extends SimpleSafeAsyncTask<DTO, Void, DTO> {
		@Override protected final DTO doInBackground(DTO param) throws Exception {
			if (!param.hasImage && currentImage != null
					&& !InventoryContract.AUTHORITY.equals(currentImage.getAuthority())) {
				param.hasImage = true;
				param.image = IOTools.readBytes(getContext().getContentResolver().openInputStream(currentImage));
			}
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
