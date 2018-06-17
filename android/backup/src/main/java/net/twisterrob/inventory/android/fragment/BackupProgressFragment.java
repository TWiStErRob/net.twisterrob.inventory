package net.twisterrob.inventory.android.fragment;

import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.*;

import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.LocalBinder;

import static net.twisterrob.inventory.android.backup.concurrent.NotificationProgressService.*;

public class BackupProgressFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupProgressFragment.class);

	private LenientProgressInfoProvider displayer;
	private ProgressBar progress;

	private View cancel;
	private View cancelWait;
	private TextView percentage;
	private TextView counts;
	private TextView description;
	private ImageView icon;

	private final BackupServiceConnection backupService = new BackupServiceConnection() {
		@Override protected void serviceBound(ComponentName name, LocalBinder service) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_PROGRESS_BROADCAST);
			filter.addAction(ACTION_FINISHED_BROADCAST);
			LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);

			if (service.isInProgress()) {
				displayer.setProgress(service.getLastProgress());
				updateUI();
				started();
			} else {
				finished();
			}
		}
		@Override protected void serviceUnbound(ComponentName name, LocalBinder service) {
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
		}
		@Override public void started() {
			setCancelling(getBinding().isCancelled());
			ViewTools.displayedIf(getView(), true);
		}
		@Override public void finished() {
			ViewTools.displayedIf(getView(), false);
			displayer.setProgress(null);
			updateUI();
		}
	};

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			Progress current = (Progress)intent.getSerializableExtra(BackupService.EXTRA_PROGRESS);
			if (cancelling != null && current != null && current.phase == Progress.Phase.Finished) {
				cancelling.dismiss();
			}
			displayer.setProgress(current);
			updateUI();
		}
	};
	private AlertDialog cancelling;

	@Override public void onAttach(Context context) {
		super.onAttach(context);
		displayer = new LenientProgressInfoProvider(context);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backup_progress, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		progress = view.findViewById(R.id.progress);
		description = view.findViewById(R.id.progress__description);
		percentage = view.findViewById(R.id.progress__percentage);
		counts = view.findViewById(R.id.progress__counts);
		icon = view.findViewById(R.id.progress__icon);
		cancel = view.findViewById(R.id.progress__cancel);
		cancelWait = view.findViewById(R.id.progress__cancel_wait);
		cancel.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				final LocalBinder binding = backupService.getBinding();
				setCancelling(true); // start displaying pending cancellation
				if (!binding.isCancellable()) {
					cancelling = DialogTools
							.confirm(v.getContext(), new PopupCallbacks<Boolean>() {
								@Override public void finished(Boolean value) {
									cancelling = null; // dialog is handled
									if (Boolean.TRUE.equals(value)) {
										binding.cancel();
									} else {
										setCancelling(false); // restore UI state
									}
								}
							})
							.setMessage(R.string.backup__cancel_partial)
							.show();
				} else {
					binding.cancel();
				}
			}
		});
	}

	@Override public void onStart() {
		super.onStart();
		// Bind at the earliest possible time and unbind at the right time (see below).
		backupService.bind(getContext());
	}
	@Override public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			// Unbind earlier, because we won't be able to handle any more stuff.
			// This helps to lessen the probability of losing progress notifications.
			backupService.unbind();
		}
	}
	@Override public void onStop() {
		super.onStop();
		if (!getActivity().isFinishing()) {
			// Unbind later than onPause. Even though the activity is paused it may still be visible.
			// For example in case of a Drive export, a nice progress is displayed by us behind the huge spinner.
			backupService.unbind();
		}
	}

	private void setCancelling(boolean cancelling) {
		ViewTools.enabledIf(cancel, !cancelling);
		ViewTools.visibleIf(cancel, !cancelling);
		ViewTools.visibleIf(cancelWait, cancelling);
	}

	private void updateUI() {
		int done = displayer.getDone();
		int total = displayer.getTotal();
		boolean indeterminate = displayer.isIndeterminate();
		icon.setImageResource(displayer.getIcon());
		progress.setMax(total);
		progress.setProgress(done);
		progress.setIndeterminate(indeterminate);
		description.setText(displayer.getMessage());
		if (!indeterminate) {
			counts.setText(String.format(Locale.getDefault(), "%d/%d", done, total));
			percentage.setText(NumberFormat.getPercentInstance().format((double)done / total));
		} else {
			counts.setText(null);
			percentage.setText(null);
		}
	}
}
