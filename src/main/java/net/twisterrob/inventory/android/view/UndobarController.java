package net.twisterrob.inventory.android.view;

import android.app.Activity;
import android.os.*;
import android.support.v4.view.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.inventory.android.R;

public class UndobarController {
	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	private static final long HIDE_DELAY = 5000;
	private static final float ALPHA_HIDE = 0;
	private static final float ALPHA_SHOW = 1;

	// View
	private View mBarView;
	private TextView mMessageView;
	private ProgressBar mProgress;
	private long toggleDuration;

	// Events
	private UndoListener mUndoListener;
	private ViewPropertyAnimatorCompat mBarAnimator;
	private CountDownTimer cdt = new CountDownTimer(HIDE_DELAY, HIDE_DELAY / 100) {
		@Override public void onTick(long millisUntilFinished) {
			float percent = millisUntilFinished / (float)HIDE_DELAY;
			mProgress.setProgress((int)(percent * 100));
		}

		@Override public void onFinish() {
			hideUndoBar(false);
		}
	};

	// State objects
	private Parcelable mUndoToken;
	private CharSequence mUndoMessage;

	public UndobarController(Activity activity) {
		ViewGroup decor = (ViewGroup)activity.getWindow().getDecorView();
		mBarView = decor.findViewById(R.id.undobar);
		if (mBarView == null) {
			activity.getLayoutInflater().inflate(R.layout.inc_undobar, decor);
			mBarView = decor.findViewById(R.id.undobar);
		}
		toggleDuration = mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime);
		mBarAnimator = ViewCompat.animate(mBarView);

		mProgress = (ProgressBar)mBarView.findViewById(R.id.undobar_progress);
		mMessageView = (TextView)mBarView.findViewById(R.id.undobar_message);
		mBarView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				hideUndoBar(true);
			}
		});
		mBarView.findViewById(R.id.undobar_button)
		        .setOnClickListener(new View.OnClickListener() {
			        @Override
			        public void onClick(View view) {
				        hideUndoBar(false);
				        mUndoListener.onUndo(mUndoToken);
			        }
		        });

		hideUndoBar(true);
	}

	public void showUndoBar(boolean immediate, CharSequence message, Parcelable undoToken, UndoListener listener) {
		cdt.cancel();
		mUndoMessage = message;
		mUndoToken = undoToken;
		mUndoListener = listener;
		mMessageView.setText(mUndoMessage);

		cdt.start();

		mBarView.setVisibility(View.VISIBLE);
		if (immediate) {
			ViewCompat.setAlpha(mBarView, ALPHA_SHOW);
		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(ALPHA_SHOW)
					.setDuration(toggleDuration)
					.setListener(null);
		}
	}

	public void hideUndoBar(boolean immediate) {
		cdt.cancel();
		if (immediate) {
			mBarView.setVisibility(View.GONE);
			ViewCompat.setAlpha(mBarView, ALPHA_HIDE);
			mUndoMessage = null;
			mUndoToken = null;
		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(ALPHA_HIDE)
					.setDuration(toggleDuration)
					.setListener(new ViewPropertyAnimatorListenerAdapter() {
						public void onAnimationEnd(View view) {
							view.setVisibility(View.GONE);
							mUndoMessage = null;
							mUndoToken = null;
						}
					});
		}
	}
}
