package net.twisterrob.inventory.android.utils;

import java.io.*;
import java.util.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.*;
import android.provider.*;

import net.twisterrob.inventory.BuildConfig;
import net.twisterrob.java.io.IOTools;

// TODO crop https://github.com/lorensiuswlt/AndroidImageCrop/blob/master/src/net/londatiga/android/MainActivity.java
// TODO crop http://code.tutsplus.com/tutorials/capture-and-crop-an-image-with-the-device-camera--mobile-11458
public class PictureUtils {
	public static final short REQUEST_CODE_GET_PICTURE = 0x41C0;
	public static final short REQUEST_CODE_TAKE_PICTURE = 0x41C1;
	public static final short REQUEST_CODE_PICK_GALLERY = 0x41C2;
	public static final short REQUEST_CODE_CROP_PICTURE = 0x41C3;

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

	public static void getPicture(Activity activity, File targetFile) {
		Uri outputFileUri = Uri.fromFile(targetFile);

		// Camera
		List<Intent> cameraIntents = new ArrayList<Intent>();
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res: listCam) {
			Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			cameraIntents.add(intent);
		}

		// Filesystem
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");

		// Chooser of filesystem options
		Intent chooserIntent = Intent.createChooser(galleryIntent, null);
		// Add the camera options
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[0]));

		activity.startActivityForResult(chooserIntent, REQUEST_CODE_GET_PICTURE);
	}

	public static void pickImageInGallery(Activity activity) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		activity.startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
	}

	public static void openImageInGallery(Activity activity, File sourceFile) {
		Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri uri = base.buildUpon().appendPath(sourceFile.getAbsolutePath()).build();
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		activity.startActivity(intent);
	}

	public static Bitmap loadPicture(String sourceFile, int targetW, int targetH) {
		Bitmap bitmap = null;
		try {
			bitmap = loadPicture(new FileInputStream(sourceFile), targetW, targetH);
		} catch (FileNotFoundException e) {
			// ignore
		}
		try {
			if (bitmap != null) {
				bitmap = rotateImage(bitmap, sourceFile);
			}
		} catch (IOException e) {
			// ignore
		}
		return bitmap;
	}
	public static Bitmap loadPicture(File sourceFile, int targetW, int targetH) {
		return loadPicture(sourceFile.getAbsolutePath(), targetW, targetH);
	}
	public static Bitmap loadPicture(Context context, Uri sourceUri, int targetW, int targetH) {
		Bitmap bitmap = null;
		InputStream stream = null;
		try {
			stream = context.getContentResolver().openInputStream(sourceUri);
			bitmap = loadPicture(stream, targetW, targetH);
		} catch (FileNotFoundException ex) {
			// ignore
		} finally {
			IOTools.ignorantClose(stream);
		}
		return bitmap;
	}
	public static Bitmap loadPicture(InputStream stream, int targetW, int targetH) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, bmOptions);

		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
		return bitmap;
	}

	public static Bitmap rotateImage(Bitmap bitmap, String filePath) throws IOException {
		Bitmap result = bitmap;
		ExifInterface exif = new ExifInterface(filePath);
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

		float rotation = getRotation(orientation);
		if (rotation != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle(); // Pretend none of this ever happened!
		}
		return result;
	}

	public static int getOrientation(Context context, Uri photoUri) {
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

		int result = ExifInterface.ORIENTATION_UNDEFINED;
		if (cursor.moveToFirst()) {
			result = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
		}
		cursor.close();
		return result;
	}

	/**
	 * @param orientation {@link ExifInterface#ORIENTATION_UNDEFINED ORIENTATION_ROTATE_*}
	 * @return degrees
	 */
	public static float getRotation(int orientation) {
		int rotation = 0;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
			rotation = 90;
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
			rotation = 180;
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
			rotation = 270;
		}
		return rotation;
	}

	public static Uri getPictureUriFromResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_GET_PICTURE) {
				boolean isCamera = false;
				if (data != null) {
					isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
				}

				Uri selectedImageUri;
				if (isCamera) {
					selectedImageUri = data != null? (Uri)data.getParcelableExtra(MediaStore.EXTRA_OUTPUT) : null;
				} else {
					selectedImageUri = data == null? null : data.getData();
				}
				return selectedImageUri;
			}
		}
		return null;
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
		if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
			return true;
		}
		return false;
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
		if (BuildConfig.DEBUG) {
			System.out.println("Authority: " + uri.getAuthority() + ", Fragment: " + uri.getFragment() + ", Port: "
					+ uri.getPort() + ", Query: " + uri.getQuery() + ", Scheme: " + uri.getScheme() + ", Host: "
					+ uri.getHost() + ", Segments: " + uri.getPathSegments().toString());
		}

		String result = null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			result = getPathKitKat(context, uri);
		}
		if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
			// Return the remote address
			if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
				result = uri.getLastPathSegment();
			} else {
				result = getDataColumn(context, uri, null, null);
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
				return getDataColumn(context, contentUri, null, null);
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

				return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
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
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		final String column = "_data";

		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, new String[]{column}, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				if (BuildConfig.DEBUG) {
					DatabaseUtils.dumpCursor(cursor);
				}

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
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
			return cropPictureCreateBitmap(file, x, y, w, h);
		} else {
			return cropPictureDecodeRegion(file, x, y, w, h);
		}
	}

	private static Bitmap cropPictureCreateBitmap(File file, int x, int y, int w, int h) {
		Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath());
		return Bitmap.createBitmap(source, x, y, w, h);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private static Bitmap cropPictureDecodeRegion(File file, int x, int y, int w, int h) throws IOException {
		String fileName = file.getAbsolutePath();

		final int l = x, t = y, r = l + w, b = t + h;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fileName, options);

		BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fileName, false);
		if (decoder != null) {
			options = new BitmapFactory.Options();
			return decoder.decodeRegion(new Rect(l, t, r, b), null);
			//			int oWidth = options.outWidth;
			//			int oHeight = options.outHeight;
			//			int startingSize = 1;
			//			if (w * oWidth * h * oHeight > 1920 * 1080) {
			//				startingSize = w * oWidth * h * oHeight / (1920 * 1080) + 1;
			//			}
			//			options.inSampleSize = startingSize;

			//			for (options.inSampleSize = startingSize; options.inSampleSize <= 32; options.inSampleSize++) {
			//				try {
			//			return decoder.decodeRegion(new Rect((int)(l * oWidth), (int)(t * oHeight), (int)(r * oWidth),
			//					(int)(b * oHeight)), options);
			//				} catch (OutOfMemoryError e) {
			//					continue; // with for loop if OutOfMemoryError occurs
			//				}
			//			}
		}
		return null;
	}

	public static void savePicture(Bitmap bitmap, File file, CompressFormat format, int quality)
			throws FileNotFoundException {
		@SuppressWarnings("resource")
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			bitmap.compress(format, quality, out);
		} finally {
			IOTools.ignorantClose(out);
		}
	}

}
