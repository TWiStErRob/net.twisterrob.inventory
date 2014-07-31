package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.google.android.gms.drive.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.CaptureImage;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.utils.*;

import static net.twisterrob.inventory.android.content.Loaders.*;
import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class ItemEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	private EditText itemName;
	private TextView itemCategory;
	private ImageView itemImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.item_edit, container, false);
		itemName = (EditText)root.findViewById(R.id.itemName);
		itemCategory = (TextView)root.findViewById(R.id.itemCategory);
		itemImage = (ImageView)root.findViewById(R.id.itemImage);

		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		return root;
	}

	@Override
	protected void onStartLoading() {
		long id = getArgItemID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());

		if (id != Item.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ITEM_ID, id);
			@SuppressWarnings("unused")
			// no dependencies yet: Item's category will come in
			Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new LoadExistingItem());
		}

		manager.startLoading();
	}

	private void save() {
		new SaveTask().execute(getCurrentItem());
	}

	private ItemDTO getCurrentItem() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		item.name = itemName.getText().toString();
		// item.category = itemCategory.getSelectedItemId(); // TODO tree ListView?
		return item;
	}

	protected File getTargetFile() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		String imageFileName = "Item_" + getArgItemID() + "_" + timeStamp + ".jpg";
		File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return new File(storageDir, imageFileName);
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getArgParentID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
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
				Intent intent = new Intent(App.getAppContext(), CaptureImage.class);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, getTargetFile().getAbsolutePath());
				startActivityForResult(intent, PictureUtils.REQUEST_CODE_TAKE_PICTURE);
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
					try {
						new Upload().execute(file);
						itemImage.setImageBitmap(PictureUtils.loadPicture(file, -1, -1));
					} catch (IOException ex) {
						LOG.error("Cannot load {}", file, ex);
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

	private final class LoadExistingItem extends LoadSingleRow {
		private LoadExistingItem() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor cursor) {
			super.process(cursor);
			ItemDTO item = ItemDTO.fromCursor(cursor);

			getActivity().setTitle(item.name);
			itemName.setText(item.name);
			itemCategory.setText(String.valueOf(item.category));
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	private final class SaveTask extends SimpleAsyncTask<ItemDTO, Void, Long> {
		@Override
		protected Long doInBackground(ItemDTO param) {
			try {
				Database db = App.getInstance().getDataBase();
				if (param.id == Item.ID_ADD) {
					return db.newItem(param.parentID, param.name, param.category);
				} else {
					db.updateItem(param.id, param.name, param.category);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				LOG.warn("Cannot save {}", param, ex);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result != null) {
				getActivity().finish();
			} else {
				Toast.makeText(getActivity(), "Item name must be unique within the item collection", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public class Upload extends ApiClientAsyncTask<File, Void, DriveFile> {
		public Upload() {
			super(getActivity());
		}

		@Override
		protected DriveFile doInBackgroundConnected(File... params) {
			try {
				return uploadFile(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(DriveFile result) {
			if (result != null) {
				Toast.makeText(App.getAppContext(), "Uploaded: " + result.getDriveId(), Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(result);
		}

		private DriveFile uploadFile(File file) throws IOException {
			DriveFolder folder = getDriveFolder();
			if (folder == null) {
				return null;
			}
			MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
					.setMimeType("image/jpeg") //
					.setTitle(file.getName()) //
					.build();
			Contents fileContents = sync(Drive.DriveApi.newContents(getGoogleApiClient()));
			DriveUtils.putFileIntoContents(fileContents, file);
			return sync(folder.createFile(getGoogleApiClient(), fileMeta, fileContents));
		}

		private DriveFolder getDriveFolder() {
			String driveFolderName = App.getPrefs().getString(Constants.Prefs.DRIVE_FOLDER_ID, null);
			if (driveFolderName != null) {
				DriveId folderId = DriveId.decodeFromString(driveFolderName);
				return Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
			}
			return null;
		}
	}

	public static ItemEditFragment newInstance(long parentID, long itemID) {
		if (parentID == Item.ID_ADD && itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Parent item ID / item ID must be provided (new item / edit item)");
		}
		if (itemID != Item.ID_ADD) { // no need to know which parent when editing
			parentID = Item.ID_ADD;
		}

		ItemEditFragment fragment = new ItemEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentID);
		args.putLong(Extras.ITEM_ID, itemID);

		fragment.setArguments(args);
		return fragment;
	}
}
