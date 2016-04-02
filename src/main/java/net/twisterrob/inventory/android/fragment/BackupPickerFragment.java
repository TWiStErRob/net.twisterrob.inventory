package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants;
import net.twisterrob.java.io.IOTools;

public class BackupPickerFragment extends DialogFragment {
	public static final String EXTRA_PATTERN = "fileRegex";
	public static final String EXTRA_TITLE = "chooserTitle";
	public static final String EXTRA_TAG = "tag";

	public interface BackupPickerListener {
		void filePicked(Serializable tag, File file);
	}

	private BackupPickerListener mListener;

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = AndroidTools.findAttachedListener(this, BackupPickerListener.class);
	}

	@Override public void onDetach() {
		mListener = null;
		super.onDetach();
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		File root = Constants.Paths.getPhoneHome();
		final File[] files = getImportableFiles(root);
		Arrays.sort(files);
		return new AlertDialog.Builder(getActivity())
				.setTitle(getArgTitle())
//				.setMessage(R.string.backup_select)
				.setItems(IOTools.getNames(files), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						File file = files[which];
						mListener.filePicked(getArgTag(), file);
					}
				})
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.create();
	}

	private String getArgTitle() {
		String title = getArguments().getString(EXTRA_TITLE);
		return title != null? title : "Select a file";
	}
	private String getArgPattern() {
		String extension = getArguments().getString(EXTRA_PATTERN);
		return extension != null? extension : "";
	}
	private Serializable getArgTag() {
		Serializable tag = getArguments().getSerializable(EXTRA_TAG);
		return tag != null? tag : "";
	}

	private File[] getImportableFiles(File root) {
		return root.listFiles(new FileFilter() {
			final Pattern pattern = Pattern.compile(getArgPattern());
			public boolean accept(File file) {
				return file.isFile() && file.canRead() && pattern.matcher(file.getName()).matches();
			}
		});
	}

	public static BackupPickerFragment choose(Serializable tag, @RegEx String pattern, CharSequence title) {
		BackupPickerFragment fragment = new BackupPickerFragment();

		Bundle args = new Bundle();
		args.putSerializable(EXTRA_TAG, tag);
		args.putCharSequence(EXTRA_TITLE, title);
		args.putString(EXTRA_PATTERN, pattern);

		fragment.setArguments(args);
		return fragment;
	}
}
