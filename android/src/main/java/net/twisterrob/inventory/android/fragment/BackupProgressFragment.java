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
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.LocalBinder;

import static net.twisterrob.inventory.android.backup.concurrent.NotificationProgressService.*;

public class BackupProgressFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupProgressFragment.class);

	private ProgressDisplayer displayer;
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
				started();
			} else {
				finished();
			}
			updateUI();
		}
		@Override protected void serviceUnbound(ComponentName name, LocalBinder service) {
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
		}
		@Override public void started() {
			setCancelling(getBinding().isCancelled());
			AndroidTools.displayedIf(getView(), true);
		}
		@Override public void finished() {
			AndroidTools.displayedIf(getView(), false);
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
		displayer = new ProgressDisplayer(context);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backup_progress, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		progress = (ProgressBar)view.findViewById(R.id.progress);
		description = (TextView)view.findViewById(R.id.progress$description);
		percentage = (TextView)view.findViewById(R.id.progress$percentage);
		counts = (TextView)view.findViewById(R.id.progress$counts);
		icon = (ImageView)view.findViewById(R.id.progress$icon);
		cancel = view.findViewById(R.id.progress$cancel);
		cancelWait = view.findViewById(R.id.progress$cancel_wait);
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
							.setMessage(R.string.backup$cancel_partial)
							.show();
				} else {
					binding.cancel();
				}
			}
		});
	}

	@Override public void onStart() {
		super.onStart();
		// bind at the earliest possible time and unbind at the latest possible time.
		backupService.bind(getContext());
	}
	@Override public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			// unbind earlier because we won't be able to handle any more stuff
			// this helps to lessen the probability of losing progress notifications
			backupService.unbind();
		}
	}
	@Override public void onStop() {
		super.onStop();
		if (!getActivity().isFinishing()) {
			// unbind later; even though the activity is paused it may still be visible
			// (in case of Drive export a nice progress is displayed behind the huge spinner)
			backupService.unbind();
		}
	}

	private void setCancelling(boolean cancelling) {
		AndroidTools.enabledIf(cancel, !cancelling);
		AndroidTools.visibleIf(cancel, !cancelling);
		AndroidTools.visibleIf(cancelWait, cancelling);
	}

	private void updateUI() {
		if (!displayer.hasProgress()) {
			LOG.debug("Not active");
			return;
		}
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
