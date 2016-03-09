package net.twisterrob.inventory.android.fragment.data;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.*;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.CursorAdapter;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.*;
import android.widget.*;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
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
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.content.model.CategorySuggester.Suggestion;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.utils.PictureHelper;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.ChangeTypeDialog.Variants;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public abstract class BaseEditFragment<T, DTO extends ImagedDTO> extends BaseSingleLoaderFragment<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseEditFragment.class);
	public static final String EDIT_IMAGE = "editImageOnStartup";
	protected static final String DYN_NameHintResource = "nameHint";
	protected static final String DYN_DescriptionHintResource = "descriptionHint";
	private static final int MAX_IMAGE_SIZE = 2048;

	private boolean isRestored;
	private Object restoredImage;
	private int restoredTypePos;

	/** byte[], Uri or null */
	private Object currentImage;
	private boolean keepNameInSync;

	private EditText name;
	private EditText description;
	private Spinner type;
	private TextView hint;
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
		if (tryRestore()) {
			return;
		}

		AndroidTools.selectByID(type, dto.type);
		name.setText(dto.name); // must set it after type to prevent keepNameInSync
		setCurrentImage(dto.hasImage? dto.getImageUri() : null); // displays image, so needs type to be selected
		description.setText(dto.description);
		if (getArguments().getBoolean(EDIT_IMAGE)) {
			getArguments().remove(EDIT_IMAGE);
			takePicture();
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
						@Override protected void update(long newType, Cursor cursor) {
							AndroidTools.selectByID(type, newType);
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
			setCurrentImage(restoredImage);
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
		if (currentImage instanceof byte[]) {
			outState.putByteArray("image", (byte[])currentImage);
		} else if (currentImage instanceof Uri) {
			outState.putParcelable("image", (Uri)currentImage);
		}
		outState.putInt("typePos", type.getSelectedItemPosition());
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			restoredImage = savedInstanceState.get("image");
			restoredTypePos = savedInstanceState.getInt("typePos");
			isRestored = true;
		}
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	// TODO maybe move overriding logic into this class
	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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

		typeImage = (ImageView)view.findViewById(R.id.type);
		AndroidTools.displayedIf(typeImage, this instanceof ItemEditFragment);

		name = (EditText)view.findViewById(R.id.title);
		name.setHint((Integer)getDynamicResource(DYN_NameHintResource));
		name.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				isClean = false;
				if (doValidateTitle()) {
					updateHint(s, false);
				}
			}
		});

		description = (EditText)view.findViewById(R.id.description);
		description.setHint((Integer)getDynamicResource(DYN_DescriptionHintResource));
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

		hint = (TextView)view.findViewById(android.R.id.hint);

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
					oldPos = position;
					isClean = false;
				}
				if (keepNameInSync) {
					boolean cleanBefore = isClean; // automatic naming shouldn't be considered a change
					super.onItemSelected(parent, view, position, id);
					isClean = cleanBefore;
				}
				reloadImage();
				updateHint(name.getText(), false);
			}
		});
		// TODO setOnItemLongClickListener is not supported, any way to work around? So user has the same "tooltip" as in ChangeTypeDialog
		type.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View view) {
				ChangeTypeDialog.showKeywords(view.getContext(), getTypeName());
				return true;
			}
		});
	}

	private void updateHint(CharSequence s, boolean forceSuggest) {
		if (!(this instanceof ItemEditFragment)) {
			return;
		}
		String suggest = App.getSPref(R.string.pref_suggestCategory, R.string.pref_suggestCategory_default);
		String always = getString(R.string.pref_suggestCategory_always);
		String unmatched = getString(R.string.pref_suggestCategory_unmatched);
		CharSequence hintText = null;
		if (always.equals(suggest) || unmatched.equals(suggest) || forceSuggest) {
			Collection<Suggestion> suggestions = CategoryDTO.getSuggester(name.getContext()).suggest(s);

			if (!suggestions.isEmpty()) {
				hintText = buildHint(suggestions, getTypeName());
				if (unmatched.equals(suggest) && !forceSuggest) {
					for (Suggestion suggestion : suggestions) {
						String currentCategoryKey = getTypeName();
						if (suggestion.getCategoryKey().equals(currentCategoryKey)) {
							hintText = null;
						}
					}
				}
			} else {
				if (forceSuggest) {
					hintText = "Can't find any matching categories, sorry.";
				}
			}
		}
		hint.setText(hintText);
		hint.setMovementMethod(LinkMovementMethod.getInstance());
		AndroidTools.displayedIfHasText(hint);
	}
	public CharSequence buildHint(@NonNull Collection<Suggestion> suggestions, String current) {
		LongSparseArray<Map<CharSequence, Collection<Suggestion>>> grouped = group(suggestions);

		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (int i = 0; i < grouped.size(); ++i) {
			Map<CharSequence, Collection<Suggestion>> matchGroup = grouped.valueAt(i);

			final Suggestion categorySuggestion = matchGroup.entrySet().iterator().next().getValue().iterator().next();
			boolean isCurrent = categorySuggestion.getCategoryKey().equals(current);

			if (isCurrent) {
				builder.append("\u2714");
			}
			int start = builder.length();
			builder.append(categorySuggestion.getCategoryPath());
			int end = builder.length();

			if (!isCurrent) {
				builder.setSpan(new ClickableSpan() {
					@Override public void onClick(View widget) {
						AndroidTools.selectByID(type, categorySuggestion.category);
					}
				}, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}

			builder.append(" (");
			for (Iterator<Entry<CharSequence, Collection<Suggestion>>> sugIt =
			     matchGroup.entrySet().iterator(); sugIt.hasNext(); ) {
				Entry<CharSequence, Collection<Suggestion>> group = sugIt.next();
				int matchStart = builder.length();
				builder.append(group.getKey());
				for (Suggestion suggestion : group.getValue()) {
					builder.setSpan(new ForegroundColorSpan(Color.RED),
							matchStart + suggestion.matchStart,
							matchStart + suggestion.matchEnd,
							Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				if (sugIt.hasNext()) {
					builder.append(", ");
				}
			}
			builder.append(")");
			if (i < grouped.size() - 1) {
				builder.append("\n");
			}
		}
		return builder;
	}
	private LongSparseArray<Map<CharSequence, Collection<Suggestion>>> group(
			@NonNull Collection<Suggestion> suggestions) {
		LongSparseArray<Map<CharSequence, Collection<Suggestion>>> grouped = new LongSparseArray<>();
		for (Suggestion suggestion : suggestions) {
			Map<CharSequence, Collection<Suggestion>> matchGroup = grouped.get(suggestion.category);
			if (matchGroup == null) {
				matchGroup = new TreeMap<>();
				grouped.put(suggestion.category, matchGroup);
			}
			Collection<Suggestion> group = matchGroup.get(suggestion.match);
			if (group == null) {
				group = new TreeSet<>(new Comparator<Suggestion>() {
					@Override public int compare(Suggestion lhs, Suggestion rhs) {
						return lhs.search.toString().compareToIgnoreCase(rhs.search.toString());
					}
				});
				matchGroup.put(suggestion.match, group);
			}
			group.add(suggestion);
		}
		return grouped;
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
				startActivity(MainActivity.list(MainActivity.PAGE_CATEGORY_HELP));
				return true;
			case R.id.action_category_suggest:
				updateHint(name.getText(), true);
				return true;
			case R.id.action_category_keywords:
				ChangeTypeDialog.showKeywords(getContext(), getTypeName());
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
		dto.hasImage = currentImage != null;
		dto.image = currentImage instanceof byte[]? (byte[])currentImage : null;
		dto.name = name.getText().toString();
		dto.description = description.getText().toString();
		dto.type = type.getSelectedItemId();
		new SaveTask().executeSerial(dto);
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

	private String getTypeName() {
		Cursor cursor = (Cursor)type.getItemAtPosition(type.getSelectedItemPosition());
		return cursor != null? cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME)) : null;
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

	private void removePicture() {
		setCurrentImage(null);
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
					Intent intent = CaptureImage.saveTo(getContext(), file, MAX_IMAGE_SIZE);
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

	@Override public void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		switch (requestCode) {
			case ImageTools.REQUEST_CODE_GET_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
					loadExternal(data.getData());
				}
				return;
			case ImageTools.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
					loadInternal(new File(data.getData().getPath()));
				}
				return;
			default:
				// do as super pleases
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void loadInternal(File file) {
		new SimpleSafeAsyncTask<File, Void, byte[]>() {
			@Override protected void onPreExecute() {
				loadTypeImage(image);
			}
			@Override protected byte[] doInBackground(File file) throws Exception {
				byte[] bytes = IOTools.readBytes(file);
				if (!file.delete()) {
					throw new IOException("Cannot remove temporary file.");
				}
				return bytes;
			}
			@Override protected void onResult(byte[] bytes, File file) {
				setCurrentImage(bytes);
			}
			@Override protected void onError(@NonNull Exception ex, File file) {
				App.toastUser(App.getError(ex, "Cannot process image in " + file));
			}
		}.executeParallel(file);
	}

	private void loadExternal(final Uri uri) {
		Glide
				.with(this)
				.load(uri)
				.asBitmap()
				.toBytes(CompressFormat.JPEG, 80)
				.format(DecodeFormat.PREFER_ARGB_8888)
				.atMost()
				.override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.skipMemoryCache(true)
				.into(new SimpleTarget<byte[]>() {
					@Override public void onLoadStarted(Drawable placeholder) {
						loadTypeImage(image);
					}
					@Override public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> ignore) {
						setCurrentImage(resource);
					}
					@Override public void onLoadFailed(Exception ex, Drawable ignore) {
						App.toastUser(App.getError(ex, "Cannot process image: " + uri));
					}
				})
		;
	}

	private void setCurrentImage(Object currentImage) {
		this.currentImage = currentImage;
		isClean = false;
		reloadImage();
	}

	private final long startTime = System.currentTimeMillis();
	private void reloadImage() {
		if (currentImage == null) {
			loadTypeImage(image);
		} else if (currentImage instanceof Uri) {
			Pic
					.jpg()
					.signature(new LongSignature(startTime)) // simulate image_time (Uri shouldn't change while editing)
					.load((Uri)currentImage)
					.into(image);
		} else if (currentImage instanceof byte[]) {
			Pic
					.baseRequest(byte[].class) // no need for signature, the byte[] doesn't change -> TODO glide#437
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(true)
					.signature(new LongSignature())
					.listener(new LoggingListener<byte[], GlideDrawable>("edit"))
					.load((byte[])currentImage)
					.into(image);
		} else {
			throw new IllegalStateException("Unrecognized image: " + currentImage);
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
