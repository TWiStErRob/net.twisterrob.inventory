package net.twisterrob.android.utils.log;

import java.io.*;
import java.util.ArrayList;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.*;
import android.content.pm.ProviderInfo;
import android.content.res.*;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.*;
import android.support.annotation.CallSuper;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public abstract class LoggingContentProvider extends ContentProvider {

	private static final Logger LOG = LoggerFactory.getLogger("ContentProvider");

	public LoggingContentProvider() {
		log("ctor");
	}

	//region Lifecycle

	@Override public void attachInfo(Context context, ProviderInfo info) {
		log("attachInfo", context, info);
		super.attachInfo(context, info);
	}

	@CallSuper
	@Override public boolean onCreate() {
		log("onCreate");
		return true;
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		log("onConfigurationChanged", newConfig);
		super.onConfigurationChanged(newConfig);
	}

	@Override public void shutdown() {
		log("shutdown");
		super.shutdown();
	}

	// endregion

	// region Files

	@Override public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		log("openFile", uri, mode);
		return super.openFile(uri, mode);
	}

	@Override public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal)
			throws FileNotFoundException {
		log("openFile", uri, mode, signal);
		return super.openFile(uri, mode, signal);
	}

	@Override public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {
		log("openAssetFile", uri, mode);
		return super.openAssetFile(uri, mode);
	}

	@Override public AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal)
			throws FileNotFoundException {
		log("openAssetFile", uri, mode, signal);
		return super.openAssetFile(uri, mode, signal);
	}

	@Override public AssetFileDescriptor openTypedAssetFile(
			Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
		log("openTypedAssetFile", uri, mimeTypeFilter, opts);
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}

	@Override public AssetFileDescriptor openTypedAssetFile(
			Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
		log("openTypedAssetFile", uri, mimeTypeFilter, opts, signal);
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts, signal);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@Override public <T> ParcelFileDescriptor openPipeHelper(
			Uri uri, String mimeType, Bundle opts, T args, PipeDataWriter<T> func) throws FileNotFoundException {
		log("openPipeHelper", uri, mimeType, opts, args, func);
		return super.openPipeHelper(uri, mimeType, opts, args, func);
	}

	// endregion

	// region Queries

	@Override public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
		log("getStreamTypes", uri, mimeTypeFilter);
		return super.getStreamTypes(uri, mimeTypeFilter);
	}

	@Override public Uri canonicalize(Uri url) {
		log("canonicalize", url);
		return super.canonicalize(url);
	}

	@Override public Uri uncanonicalize(Uri url) {
		log("uncanonicalize", url);
		return super.uncanonicalize(url);
	}

	@Override public int bulkInsert(Uri uri, ContentValues[] values) {
		log("bulkInsert", uri, values);
		return super.bulkInsert(uri, values);
	}

	@Override public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		log("applyBatch", operations);
		return super.applyBatch(operations);
	}

	// endregion

	@Override public Bundle call(String method, String arg, Bundle extras) {
		log("call", method, arg, extras);
		return super.call(method, arg, extras);
	}

	@Override public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
		log("dump", fd, writer, args);
		super.dump(fd, writer, args);
	}

	@Override public void onLowMemory() {
		log("onLowMemory");
		super.onLowMemory();
	}

	@Override public void onTrimMemory(int level) {
		log("onTrimMemory", StringerTools.toTrimMemoryString(level));
		super.onTrimMemory(level);
	}

	protected void log(String name, Object... args) {
		LoggingHelper.log(LOG, getName(), name, null, args);
	}

	protected String getName() {
		return getClass().getSimpleName() + "@" + StringerTools.hashString(this);
	}
}
