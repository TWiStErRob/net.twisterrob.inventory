package net.twisterrob.inventory.android.fragment;

import java.text.NumberFormat;

import org.slf4j.*;

import android.content.*;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.utils.log.*;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.Progress.Phase;
import net.twisterrob.inventory.android.backup.concurrent.BackupService;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.LocalBinder;

import static net.twisterrob.inventory.android.backup.concurrent.NotificationProgressService.*;

public class BackupProgressFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupProgressFragment.class);

	private ProgressDisplayer displayer;
	private ProgressBar progress;
	private LocalBinder binder;

	private View cancel;
	private View cancelWait;
	private TextView percentage;
	private TextView counts;
	private TextView description;
	private ImageView icon;
	private Intent serviceIntent;

	private final ServiceConnection connection = new LoggingServiceConnection() {
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			super.onServiceConnected(name, service);
			binder = (LocalBinder)service;
			if (!displayer.hasProgress() && binder.isInProgress()) {
				displayer.setProgress(binder.getLastProgress());
			}
			updateUI();
			setCancellable(true);
		}

		@Override public void onServiceDisconnected(ComponentName name) {
			super.onServiceDisconnected(name);
			binder = null;
			updateUI();
		}
	};

	private final BroadcastReceiver receiver = new LoggingBroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			//super.onReceive(context, intent); // too slow, export doesn't catch up until finished
			if (binder == null) {
				throw new IllegalStateException("No binding found when received " + AndroidTools.toString(intent));
			}
			Progress current = (Progress)intent.getSerializableExtra(BackupService.EXTRA_PROGRESS);
			displayer.setProgress(current);
			updateUI();
		}
	};

	@Override public void onAttach(Context context) {
		super.onAttach(context);
		displayer = new ProgressDisplayer(context);
		serviceIntent = new Intent(context, BackupService.class);
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
				setCancellable(false);
				binder.cancel();
			}
		});
	}

	@Override public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PROGRESS_BROADCAST);
		filter.addAction(ACTION_FINISHED_BROADCAST);
		getContext().bindService(serviceIntent, connection, BIND_DEBUG_UNBIND | BIND_AUTO_CREATE);
		LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
	}

	@Override public void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
		getContext().unbindService(connection);
	}

	private void setCancellable(boolean cancellable) {
		AndroidTools.enabledIf(cancel, cancellable);
		AndroidTools.visibleIf(cancel, cancellable);
		AndroidTools.visibleIf(cancelWait, !cancellable);
	}

	private void updateUI() {
		boolean active = binder != null && displayer.hasProgress();
		AndroidTools.displayedIf(getView(), active);
		if (!active) {
			LOG.debug("Not active: binder={}, progress={}", binder, displayer.hasProgress());
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
			counts.setText(String.format("%d/%d", done, total));
			percentage.setText(NumberFormat.getPercentInstance().format((double)done / total));
		} else {
			counts.setText(null);
			percentage.setText(null);
		}
		if (displayer.getProgress().phase == Phase.Finished) {
			displayer.displayFinishMessage();
			// Hack to hide fragment even when the service is not stopped.
			// STOPSHIP This is not enough, backing out from BackupActivity brings up the notification again
			AndroidTools.displayedIf(getView(), false);
		}
	}
}
