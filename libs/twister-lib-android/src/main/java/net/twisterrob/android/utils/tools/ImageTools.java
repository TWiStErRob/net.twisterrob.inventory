package net.twisterrob.android.utils.tools;

import java.io.*;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.Bitmap.*;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import net.twisterrob.java.io.IOTools;

// CONSIDER crop https://github.com/lorensiuswlt/AndroidImageCrop/blob/master/src/net/londatiga/android/MainActivity.java
// CONSIDER crop http://code.tutsplus.com/tutorials/capture-and-crop-an-image-with-the-device-camera--mobile-11458
@SuppressWarnings("unused")
public /*static*/ abstract class ImageTools {
	public static final short REQUEST_CODE_GET_PICTURE = 0x41C0;
	public static final short REQUEST_CODE_TAKE_PICTURE = 0x41C1;
	public static final short REQUEST_CODE_PICK_GALLERY = 0x41C2;
	public static final short REQUEST_CODE_CROP_PICTURE = 0x41C3;
	private static final String NO_SORT = null;
	private static final String NO_SELECTION = null;
	private static final String[] NO_ARGS = null;

	public static boolean takePicture(Activity activity, File targetFile) {
		if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (intent.resolveActivity(activity.getPackageManager()) != null) {
				if (targetFile != null) {
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(targetFile));
				}
				activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
				return true;
			}
		}
		return false;
	}

	public static List<Intent> createCameraIntents(Context context, File file) {
		Uri outputFileUri = Uri.fromFile(file);
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		List<Intent> cameraIntents = AndroidTools.resolveIntents(context, captureIntent, 0);
		for (Intent intent : cameraIntents) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		}
		return cameraIntents;
	}

	public static Intent createGalleryIntent() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		return galleryIntent;
	}

	public static @NonNull Intent createCaptureIntent(Activity activity, File targetFile) {
		// Chooser of filesystem options
		Intent chooserIntent = Intent.createChooser(createGalleryIntent(), null);
		// Add the camera options
		List<Intent> camIntents = createCameraIntents(activity, targetFile);
		return AndroidTools.setInitialIntents(chooserIntent, camIntents);
	}

	public static void getPicture(Activity activity, File targetFile) {
		activity.startActivityForResult(createCaptureIntent(activity, targetFile), REQUEST_CODE_GET_PICTURE);
	}
	public static void pickImageInGallery(Activity activity) {
		activity.startActivityForResult(createGalleryIntent(), REQUEST_CODE_PICK_GALLERY);
	}

	public static void openImageInGallery(Activity activity, File sourceFile) {
		Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri uri = base.buildUpon().appendPath(sourceFile.getAbsolutePath()).build();
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		activity.startActivity(intent);
	}

	@SuppressWarnings("resource")
	public static Bitmap loadPicture(String sourceFile, int targetW, int targetH) throws IOException {
		Bitmap bitmap = null;
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(sourceFile);
			bitmap = loadPicture(stream, targetW, targetH);
		} finally {
			IOTools.ignorantClose(stream);
		}
		if (bitmap != null) {
			int rotation = getExifRotation(new File(sourceFile));
			if (rotation != -1) {
				bitmap = rotateImage(bitmap, rotation);
			}
		}
		return bitmap;
	}
	public static Bitmap loadPicture(File sourceFile, int targetW, int targetH) throws IOException {
		return loadPicture(sourceFile.getAbsolutePath(), targetW, targetH);
	}
	public static Bitmap loadPicture(Context context, Uri sourceUri, int targetW, int targetH) throws IOException {
		Bitmap bitmap = null;
		InputStream stream = null;
		try {
			stream = context.getContentResolver().openInputStream(sourceUri);
			bitmap = loadPicture(stream, targetW, targetH);
		} finally {
			IOTools.ignorantClose(stream);
		}
		return bitmap;
	}
	public static Bitmap loadPicture(InputStream stream, int targetW, int targetH) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		if (targetW > 0 && targetH > 0) {
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(stream, null, bmOptions);

			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
			bmOptions.inSampleSize = Math.min(photoW / targetW, photoH / targetH);
			bmOptions.inJustDecodeBounds = false;
		}

		return BitmapFactory.decodeStream(stream, null, bmOptions);
	}

	public static Bitmap loadPicture(Context context, int drawableResourceID) {
		return drawableToBitmap(ContextCompat.getDrawable(context, drawableResourceID));
	}
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		if (width <= 0) {
			width = 1;
		}
		int height = drawable.getIntrinsicHeight();
		if (height <= 0) {
			height = 1;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static Bitmap rotateImage(Bitmap bitmap, int rotation) {
		Bitmap result = bitmap;
		if (rotation != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle(); // Pretend none of this ever happened!
		}
		return result;
	}

	public static int getOrientation(Context context, Uri photoUri) {
		@SuppressWarnings("resource") Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] {MediaStore.Images.ImageColumns.ORIENTATION}, NO_SELECTION, NO_ARGS, NO_SORT);
		int result = ExifInterface.ORIENTATION_UNDEFINED;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					result = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
				}
			} finally {
				cursor.close();
			}
		}
		return result;
	}

	public static int getExifRotation(File file) throws IOException {
		return getExifRotation(getExifOrientation(file));
	}

	public static int getExifOrientation(File file) throws IOException {
		ExifInterface exif = new ExifInterface(file.getAbsolutePath());
		return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
	}

	public static int getExifRotation(int orientation) {
		switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
			case ExifInterface.ORIENTATION_UNDEFINED:
				return 0;
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			default:
				return -1;
		}
	}

	public static String getExifString(int orientation) {
		switch (orientation) {
			case ExifInterface.ORIENTATION_UNDEFINED:
				return "UNDEFINED";
			case ExifInterface.ORIENTATION_NORMAL:
				return "NORMAL";
			case ExifInterface.ORIENTATION_ROTATE_90:
				return "ROTATE_90";
			case ExifInterface.ORIENTATION_ROTATE_180:
				return "ROTATE_180";
			case ExifInterface.ORIENTATION_ROTATE_270:
				return "ROTATE_270";
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				return "FLIP_HOR";
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				return "FLIP_VER";
			case ExifInterface.ORIENTATION_TRANSPOSE:
				return "TRANSPOSE";
			case ExifInterface.ORIENTATION_TRANSVERSE:
				return "TRANSVERSE";
			default:
				return "unknown";
		}
	}

	public static Uri getPictureUriFromResult(int requestCode, int resultCode, Intent data, Uri fallback) {
		Uri selectedImageUri = fallback;
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_PICTURE && data != null) {
			boolean isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
			if (isCamera) {
				selectedImageUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
			} else {
				selectedImageUri = data.getData();
			}
		}
		return selectedImageUri;
	}

	/**
	 * Convert Uri into File, if possible.
	 *
	 * @return file A local file that the Uri was pointing to, or null if the
	 *         Uri is unsupported or pointed to a remote resource.
	 * @see #getPath(Context, Uri)
	 * @author paulburke
	 */
	public static File getFile(Context context, Uri uri) {
		if (uri != null) {
			String path = getPath(context, uri);
			if (path != null && isLocal(path)) {
				return new File(path);
			}
		}
		return null;
	}
	/**
	 * @return Whether the URI is a local one.
	 */
	public static boolean isLocal(String url) {
		return url != null && !url.startsWith("http://") && !url.startsWith("https://");
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.<br>
	 * <br>
	 * Callers should check whether the path is local before assuming it
	 * represents a local file.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @see #isLocal(String)
	 * @see #getFile(Context, Uri)
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {
		//LOG.debug("Authority: " + uri.getAuthority() //
		//		+ ", Fragment: " + uri.getFragment() //
		//		+ ", Port: " + uri.getPort() //
		//		+ ", Query: " + uri.getQuery() //
		//		+ ", Scheme: " + uri.getScheme() //
		//		+ ", Host: " + uri.getHost() //
		//		+ ", Segments: " + uri.getPathSegments().toString());

		String result = null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			result = getPathKitKat(context, uri);
		}
		if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
			if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
				result = uri.getLastPathSegment();
			} else {
				result = getDataColumn(context, uri, NO_SELECTION, NO_ARGS);
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
			result = uri.getPath();
		}

		return result;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static String getPathKitKat(final Context context, final Uri uri) {
		if (DocumentsContract.isDocumentUri(context, uri)) { // DocumentProvider
			if ("com.android.externalstorage.documents".equals(uri.getAuthority())) { // ExternalStorageProvider
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			} else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) { // DownloadsProvider
				String id = DocumentsContract.getDocumentId(uri);
				Uri base = Uri.parse("content://downloads/public_downloads");
				Uri contentUri = ContentUris.withAppendedId(base, Long.valueOf(id));
				return getDataColumn(context, contentUri, NO_SELECTION, NO_ARGS);
			} else if ("com.android.providers.media.documents".equals(uri.getAuthority())) { // MediaProvider
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				return getDataColumn(context, contentUri, "_id=?", split[1]);
			}
		}
		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 * @author paulburke
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String... selectionArgs) {
		final String column = "_data";
		Cursor cursor = null;
		try {
			//noinspection resource cursor is closed nicely
			cursor = context.getContentResolver().query(uri, new String[] {column}, selection, selectionArgs, NO_SORT);
			if (cursor != null && cursor.moveToFirst()) {
				//DatabaseTools.dumpCursor(cursor);
				return cursor.getString(cursor.getColumnIndexOrThrow(column));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public static Bitmap cropPicture(File file, int x, int y, int w, int h) throws IOException {
		return crop(file, new Rect(x, y, x + w, y + h));
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public static Bitmap cropPicture(File file, float left, float top, float right, float bottom) throws IOException {
		RectF perc = new RectF();
		int orientation = getExifOrientation(file);
		switch (orientation) {
			default:
			case ExifInterface.ORIENTATION_UNDEFINED:
			case ExifInterface.ORIENTATION_NORMAL:
				perc.left = left;
				perc.top = top;
				perc.right = right;
				perc.bottom = bottom;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				perc.left = top;
				perc.top = 1 - right;
				perc.right = bottom;
				perc.bottom = 1 - left;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				perc.left = 1 - right;
				perc.top = 1 - bottom;
				perc.right = 1 - left;
				perc.bottom = 1 - top;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				perc.left = 1 - bottom;
				perc.top = left;
				perc.right = 1 - top;
				perc.bottom = right;
				break;
		}
		BitmapFactory.Options options = getSizeInternal(file);

		Rect rect = new Rect();
		rect.left = (int)(perc.left * options.outWidth);
		rect.top = (int)(perc.top * options.outHeight);
		rect.right = (int)(perc.right * options.outWidth);
		rect.bottom = (int)(perc.bottom * options.outHeight);

		//LOG.debug("cropPicture({}, {}, {}, {}, {}) --{}--> {} --{}x{}--> {}", file.getName(), left, top, right, bottom,
		//		getExifString(orientation), perc, options.outWidth, options.outHeight, rect);

		Bitmap crop = crop(file, rect);
		return rotateImage(crop, getExifRotation(orientation));
	}

	public static Bitmap crop(File file, Rect rect) throws IOException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
			return cropBitmap(file, rect);
		} else {
			return cropRegion(file, rect);
		}
	}

	private static Bitmap cropBitmap(File file, Rect rect) {
		Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), cropOptions());
		return Bitmap.createBitmap(source, rect.left, rect.top, rect.width(), rect.height());
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private static Bitmap cropRegion(File file, Rect rect) throws IOException {
		BitmapRegionDecoder decoder = null;
		try {
			decoder = BitmapRegionDecoder.newInstance(file.getAbsolutePath(), true);
			if (decoder != null) {
				return decoder.decodeRegion(rect, cropOptions());
			}
		} finally {
			if (decoder != null) {
				decoder.recycle();
			}
		}
		return null;
	}

	private static Options cropOptions() {
		Options options = new Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inDither = false;
		if (Build.VERSION_CODES.GINGERBREAD_MR1 <= Build.VERSION.SDK_INT) {
			options.inPreferQualityOverSpeed = true;
		}
		return options;
	}

	private static BitmapFactory.Options getSizeInternal(File file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		return options;
	}

	public static int[] getSize(File file) {
		BitmapFactory.Options options = getSizeInternal(file);
		return new int[] {options.outWidth, options.outHeight};
	}

	//	private static Bitmap cropPictureDecodeRegion(File file, int x, int y, int w, int h) throws IOException {
	//			int oWidth = options.outWidth;
	//			int oHeight = options.outHeight;
	//			int startingSize = 1;
	//			if (w * oWidth * h * oHeight > 1920 * 1080) {
	//				startingSize = w * oWidth * h * oHeight / (1920 * 1080) + 1;
	//			}
	//			options = new BitmapFactory.Options();
	//			options.inSampleSize = startingSize;

	//			for (options.inSampleSize = startingSize; options.inSampleSize <= 32; options.inSampleSize++) {
	//				try {
	//			return decoder.decodeRegion(new Rect((int)(l * oWidth), (int)(t * oHeight), (int)(r * oWidth),
	//					(int)(b * oHeight)), options);
	//				} catch (OutOfMemoryError e) {
	//					continue; // with for loop if OutOfMemoryError occurs
	//				}
	//			}
	//		return null;
	//	}

	public static void savePicture(Bitmap bitmap, File file, CompressFormat format, int quality) throws IOException {
		@SuppressWarnings("resource")
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			bitmap.compress(format, quality, out);
			out.flush();
		} finally {
			IOTools.ignorantClose(out);
		}
	}

	public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}

	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float)height / (float)reqHeight);
			} else {
				inSampleSize = Math.round((float)width / (float)reqWidth);
			}
		}

		return inSampleSize;
	}

	protected ImageTools() {
		// static utility class
	}

	/**
	 * @see com.bumptech.glide.load.resource.bitmap.TransformationUtils#fitCenter
	 */
	public static Bitmap downscale(Bitmap source, int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Non-positive sizes are not allowed.");
		}
		if (source.getWidth() == width && source.getHeight() == height) {
			return source;
		}
		final float widthPercentage = width / (float)source.getWidth();
		final float heightPercentage = height / (float)source.getHeight();
		final float minPercentage = Math.min(widthPercentage, heightPercentage);

		final int targetWidth = Math.round(minPercentage * source.getWidth());
		final int targetHeight = Math.round(minPercentage * source.getHeight());

		if (source.getWidth() <= targetWidth && source.getHeight() <= targetHeight) {
			return source;
		}

		Bitmap.Config config = source.getConfig() != null? source.getConfig() : Bitmap.Config.ARGB_8888;
		Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, config);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			result.setHasAlpha(source.hasAlpha());
		}

		Canvas canvas = new Canvas(result);
		Matrix matrix = new Matrix();
		matrix.setScale(minPercentage, minPercentage);
		Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(source, matrix, paint);

		return result;
	}
}
