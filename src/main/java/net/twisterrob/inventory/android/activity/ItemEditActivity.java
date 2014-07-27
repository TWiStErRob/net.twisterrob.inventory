package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.utils.*;

import static net.twisterrob.inventory.android.content.Loaders.*;
import static net.twisterrob.inventory.android.utils.DriveUtils.*;
public class ItemEditActivity extends BaseEditActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditActivity.class);

	private EditText itemName;
	private TextView itemCategory;
	private ImageView itemImage;
	private long itemID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		itemID = getIntent().getLongExtra(Extras.ITEM_ID, Item.ID_ADD);

		super.setContentView(R.layout.item_edit);
		itemName = (EditText)findViewById(R.id.itemName);
		itemCategory = (TextView)findViewById(R.id.itemCategory);
		itemImage = (ImageView)findViewById(R.id.itemImage);

		DynamicLoaderManager manager = new DynamicLoaderManager(getSupportLoaderManager());
		Bundle args = new Bundle();
		args.putLong(Extras.ITEM_ID, itemID);
		Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new LoadExistingItem());
		Dependency<Void> loadItemCondition = manager.add(-SingleItem.ordinal(), args, new IsExistingItem());

		loadItemData.dependsOn(loadItemCondition);
		manager.startLoading();
	}

	protected File getTargetFile() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		String imageFileName = "Item_" + itemID + "_" + timeStamp + ".jpg";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return new File(storageDir, imageFileName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_picture_take:
				Intent intent = new Intent(getApplicationContext(), CaptureImage.class);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PictureUtils.REQUEST_CODE_GET_PICTURE:
			case PictureUtils.REQUEST_CODE_TAKE_PICTURE:
				if (resultCode == Activity.RESULT_OK && data != null) {
					File file = PictureUtils.getFile(this, data.getData());
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

	private final class IsExistingItem extends DynamicLoaderManager.Condition {
		private IsExistingItem() {
			super(ItemEditActivity.this);
		}

		@Override
		protected boolean test(int id, Bundle args) {
			return args != null && args.getLong(Extras.ITEM_ID, Item.ID_ADD) != Item.ID_ADD;
		}
	}

	private final class LoadExistingItem extends LoadSingleRow {
		private LoadExistingItem() {
			super(ItemEditActivity.this);
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndex(Item.NAME));
			String category = item.getString(item.getColumnIndex(Item.CATEGORY));

			setTitle(name);
			itemName.setText(name);
			itemCategory.setText(category);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			finish();
		}
	}

	public class Upload extends ApiClientAsyncTask<File, Void, DriveFile> {
		public Upload() {
			super(ItemEditActivity.this);
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
				Toast.makeText(getApplicationContext(), "Uploaded: " + result.getDriveId(), Toast.LENGTH_LONG).show();
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
}
