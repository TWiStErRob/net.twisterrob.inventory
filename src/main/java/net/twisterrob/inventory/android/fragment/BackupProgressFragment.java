package net.twisterrob.inventory.android.fragment;

import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.*;

import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.*;

import static net.twisterrob.inventory.android.backup.concurrent.BackupService.*;

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
			if (!displayer.hasProgress() && service.isInProgress()) {
				displayer.setProgress(service.getLastProgress());
			}
			updateUI();
		}
		@Override public void started() {
			setCancellable(true);
			AndroidTools.displayedIf(getView(), true);
		}
		@Override public void finished() {
			AndroidTools.displayedIf(getView(), false);
		}
	};

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			Progress current = (Progress)intent.getSerializableExtra(BackupService.EXTRA_PROGRESS);
			displayer.setProgress(current);
			if (displayer.hasProgress() && displayer.getProgress().phase == Progress.Phase.Finished) {
				LOG.debug("Delegating to onNewIntent: {}", current);
				// @see BackupService.createFinishedIntent
				Intent activityIntent = new Intent(getContext(), getActivity().getClass());
				activityIntent.putExtra(EXTRA_PROGRESS, current);
				startActivity(activityIntent);
				return;
			}
			updateUI();
		}
	};

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
				setCancellable(false);
				backupService.getBinding().cancel();
			}
		});
	}

	@Override public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PROGRESS_BROADCAST);
		filter.addAction(ACTION_FINISHED_BROADCAST);
		backupService.bind(getContext());
		LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
	}

	@Override public void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
		backupService.unbind();
	}

	private void setCancellable(boolean cancellable) {
		AndroidTools.enabledIf(cancel, cancellable);
		AndroidTools.visibleIf(cancel, cancellable);
		AndroidTools.visibleIf(cancelWait, !cancellable);
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
