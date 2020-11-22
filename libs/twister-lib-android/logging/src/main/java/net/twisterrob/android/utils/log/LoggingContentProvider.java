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

import androidx.annotation.*;

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

	@Override public @Nullable ParcelFileDescriptor openFile(
			@NonNull Uri uri,
			@NonNull String mode
	) throws FileNotFoundException {
		log("openFile", uri, mode);
		return super.openFile(uri, mode);
	}

	@Override public @Nullable ParcelFileDescriptor openFile(
			@NonNull Uri uri,
			@NonNull String mode,
			@Nullable CancellationSignal signal
	) throws FileNotFoundException {
		log("openFile", uri, mode, signal);
		return super.openFile(uri, mode, signal);
	}

	@Override public @Nullable AssetFileDescriptor openAssetFile(
			@NonNull Uri uri,
			@NonNull String mode
	) throws FileNotFoundException {
		log("openAssetFile", uri, mode);
		return super.openAssetFile(uri, mode);
	}

	@Override public @Nullable AssetFileDescriptor openAssetFile(
			@NonNull Uri uri,
			@NonNull String mode,
			@Nullable CancellationSignal signal
	) throws FileNotFoundException {
		log("openAssetFile", uri, mode, signal);
		return super.openAssetFile(uri, mode, signal);
	}

	@Override public @Nullable AssetFileDescriptor openTypedAssetFile(
			@NonNull Uri uri,
			@NonNull String mimeTypeFilter,
			@Nullable Bundle opts
	) throws FileNotFoundException {
		log("openTypedAssetFile", uri, mimeTypeFilter, opts);
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}

	@Override public @Nullable AssetFileDescriptor openTypedAssetFile(
			@NonNull Uri uri,
			@NonNull String mimeTypeFilter,
			@Nullable Bundle opts,
			@Nullable CancellationSignal signal
	) throws FileNotFoundException {
		log("openTypedAssetFile", uri, mimeTypeFilter, opts, signal);
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts, signal);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@Override public @NonNull <T> ParcelFileDescriptor openPipeHelper(
			@NonNull Uri uri,
			@NonNull String mimeType, 
			@Nullable Bundle opts,
			@Nullable T args,
			@NonNull PipeDataWriter<T> func
	) throws FileNotFoundException {
		log("openPipeHelper", uri, mimeType, opts, args, func);
		return super.openPipeHelper(uri, mimeType, opts, args, func);
	}

	// endregion

	// region Queries

	@Override public @Nullable String[] getStreamTypes(@NonNull Uri uri, @NonNull String mimeTypeFilter) {
		log("getStreamTypes", uri, mimeTypeFilter);
		return super.getStreamTypes(uri, mimeTypeFilter);
	}

	@Override public @Nullable Uri canonicalize(@NonNull Uri url) {
		log("canonicalize", url);
		return super.canonicalize(url);
	}

	@Override public @Nullable Uri uncanonicalize(@NonNull Uri url) {
		log("uncanonicalize", url);
		return super.uncanonicalize(url);
	}

	@Override public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		log("bulkInsert", uri, values);
		return super.bulkInsert(uri, values);
	}

	@Override public @NonNull ContentProviderResult[] applyBatch(
			@NonNull ArrayList<ContentProviderOperation> operations
	) throws OperationApplicationException {
		log("applyBatch", operations);
		return super.applyBatch(operations);
	}

	// endregion

	@Override public @Nullable Bundle call(
			@NonNull String method,
			@Nullable String arg,
			@Nullable Bundle extras
	) {
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

	protected void log(@NonNull String name, @NonNull Object... args) {
		LoggingHelper.log(LOG, getName(), name, null, args);
	}

	protected @NonNull String getName() {
		return getClass().getSimpleName() + "@" + StringerTools.hashString(this);
	}
}
