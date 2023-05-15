package net.twisterrob.inventory.android.fragment;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.twisterrob.android.utils.tools.DialogTools;
import net.twisterrob.inventory.android.Constants;
import net.twisterrob.inventory.android.backup.R;
import net.twisterrob.inventory.android.backup.concurrent.BackupService;
import net.twisterrob.inventory.android.content.InventoryContract;

public class BackupControllerFragment extends BaseFragment<BackupControllerFragment.BackupEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupControllerFragment.class);

	public BackupControllerFragment() {
		super(R.layout.fragment_backup_controller);
		setDynamicResource(DYN_EventsClass, BackupEvents.class);
	}

	public interface BackupEvents {
		void ensureNotInProgress();
	}

	private final ActivityResultLauncher<String> exporter = registerForActivityResult(
			new ActivityResultContracts.CreateDocument(InventoryContract.Export.TYPE_BACKUP) {
				@Override public @NonNull Intent createIntent(@NonNull Context context, @NonNull String input) {
					return super
							.createIntent(context, input)
							.addCategory(Intent.CATEGORY_OPENABLE);
				}
			},
			new ActivityResultCallback<Uri>() {
				@Override public void onActivityResult(@Nullable Uri result) {
					if (result != null) {
						doExport(result);
					} else {
						LOG.info("Null result received, hopefully user cancelled export.");
					}
				}
			}
	);

	private final ActivityResultLauncher<String[]> importer = registerForActivityResult(
			new ActivityResultContracts.OpenDocument() {
				@Override public @NonNull Intent createIntent(@NonNull Context context, @NonNull String[] input) {
					return super
							.createIntent(context, input)
							.addCategory(Intent.CATEGORY_OPENABLE);
				}
			},
			new ActivityResultCallback<Uri>() {
				@Override public void onActivityResult(@Nullable Uri result) {
					if (result != null) {
						doImport(result);
					} else {
						LOG.info("Null result received, hopefully user cancelled import.");
					}
				}
			}
	);

	private Button importButton;
	private Button exportButton;
	private Button sendButton;

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		importButton = view.findViewById(R.id.backup_import);
		importButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				handleImport();
			}
		});
		exportButton = view.findViewById(R.id.backup_export);
		exportButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				handleExport();
			}
		});
		sendButton = view.findViewById(R.id.backup_send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				handleSend();
			}
		});
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		importButton = null;
		exportButton = null;
		sendButton = null;
	}

	public void onAllowNewChanged(boolean isAllowNew) {
		importButton.setEnabled(isAllowNew);
		exportButton.setEnabled(isAllowNew);
		sendButton.setEnabled(isAllowNew);
	}

	private void handleImport() {
		DialogTools
				.confirm(requireActivity(), new DialogTools.PopupCallbacks<Boolean>() {
					@Override public void finished(Boolean value) {
						if (Boolean.TRUE.equals(value)) {
							startImport();
						}
					}
				})
				.setTitle(R.string.backup_import_confirm_title)
				.setMessage(getString(R.string.backup_import_confirm_warning, "a backup"))
				.show();
	}

	private void startImport() {
		eventsListener.ensureNotInProgress();
		importer.launch(new String[] {InventoryContract.Export.TYPE_BACKUP});
	}

	private void doImport(@NonNull Uri source) {
		eventsListener.ensureNotInProgress();
		Intent intent = new Intent(BackupService.ACTION_IMPORT, source, requireContext(), BackupService.class);
		BackupService.enqueueWork(requireContext(), intent);
	}

	private void handleExport() {
		startExport();
	}

	private void startExport() {
		eventsListener.ensureNotInProgress();
		Calendar now = Calendar.getInstance();
		exporter.launch(Constants.Paths.getExportFileName(now));
	}

	private void doExport(@NonNull Uri result) {
		Intent intent = new Intent(BackupService.ACTION_EXPORT, result, requireContext(), BackupService.class);
		BackupService.enqueueWork(requireContext(), intent);
	}

	private void handleSend() {
		DialogTools
				.confirm(requireActivity(), new DialogTools.PopupCallbacks<Boolean>() {
					@Override public void finished(Boolean value) {
						if (Boolean.TRUE.equals(value)) {
							startSend();
						}
					}
				})
				.setTitle(R.string.backup_export_external_confirm_title)
				.setMessage(R.string.backup_export_external_confirm_warning)
				.show();
	}

	private void startSend() {
		eventsListener.ensureNotInProgress();
		Calendar now = Calendar.getInstance();
		Intent intent = new Intent(Intent.ACTION_SEND)
				.setType(InventoryContract.Export.TYPE_BACKUP)
				.putExtra(Intent.EXTRA_STREAM, InventoryContract.Export.getUri(now))
				.putExtra(Intent.EXTRA_SUBJECT, Constants.Paths.getExportFileName(now))
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// this makes the user choose a target and that target will call the provider later, which starts the service
		startActivity(Intent.createChooser(intent, getString(R.string.backup_send)));
		// alternatives would be:
		// ACTION_PICK_ACTIVITY which calls onActivityResult, but it looks ugly
		// createChooser(..., PendingIntent.getBroadcast.getIntentSender), but it's API 22 and only notifies after started
	}
}
