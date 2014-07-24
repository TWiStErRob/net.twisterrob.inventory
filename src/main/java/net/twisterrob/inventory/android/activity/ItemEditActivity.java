package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.*;
import android.view.*;
import android.widget.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.utils.*;

import static net.twisterrob.inventory.android.content.Loaders.*;
public class ItemEditActivity extends BaseEditActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditActivity.class);

	private PictureHelper helper;

	private EditText itemName;
	private TextView itemCategory;
	private ImageView itemImage;
	private long itemID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		itemID = getIntent().getLongExtra(Extras.ITEM_ID, Item.ID_ADD);

		helper = new PictureHelper(this) {
			@Override
			protected File getTargetFile() {
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
				String imageFileName = "Item_" + itemID + "_" + timeStamp + ".jpg";
				File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
				return new File(storageDir, imageFileName);
			}
		};

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_picture_pick:
				startActivityForResult(helper.startCapture(), PictureUtils.REQUEST_CODE_GET_PICTURE);
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
				if (helper.endCapture(resultCode, data)) {
					Intent crop = helper.startCrop();
					if (crop != null) {
						startActivityForResult(crop, PictureUtils.REQUEST_CODE_CROP_PICTURE);
					} else {
						imageReceived(); // use uncropped image
					}
				}
				break;
			case PictureUtils.REQUEST_CODE_CROP_PICTURE:
				helper.endCrop(resultCode, data);
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	private void imageReceived() {
		LOG.debug("Result: {}", helper.getFile());
		itemImage.setImageBitmap(helper.getThumbnail());
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
}
