package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.java.io.IOTools;

public class BackupPickerFragment extends DialogFragment {
	public static final String EXTRA_EXTENSION = "fileExtension";
	public static final String EXTRA_TITLE = "chooserTitle";

	public interface BackupPickerListener {
		void filePicked(File file);
	}

	private BackupPickerListener mListener;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = AndroidTools.getAttachedFragmentListener(activity, BackupPickerListener.class);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		File root = new File(App.getInstance().getPhoneHome(), Constants.EXPORT_SDCARD_FOLDER);
		final File[] files = getImportableFiles(root);
		Arrays.sort(files);
		return new AlertDialog.Builder(getActivity()) //
				.setTitle(getArgTitle()) //
				.setItems(IOTools.getNames(files), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						File file = files[which];
						mListener.filePicked(file);
					}
				}) //
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}) //
				.create();
	}

	private String getArgTitle() {
		String title = getArguments().getString(EXTRA_TITLE);
		return title != null? title : "Select a file";
	}
	private String getArgExtension() {
		String extension = getArguments().getString(EXTRA_EXTENSION);
		return extension != null? extension : "";
	}

	private File[] getImportableFiles(File root) {
		return root.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && file.canRead()
						&& file.getName().toLowerCase(Locale.getDefault()).endsWith(getArgExtension());
			}
		});
	}

	public static BackupPickerFragment choose(String title, String extension) {
		BackupPickerFragment fragment = new BackupPickerFragment();

		Bundle args = new Bundle();
		args.putString(EXTRA_TITLE, title);
		args.putString(EXTRA_EXTENSION, extension);

		fragment.setArguments(args);
		return fragment;
	}
}