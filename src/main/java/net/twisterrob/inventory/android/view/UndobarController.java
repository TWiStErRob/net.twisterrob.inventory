package net.twisterrob.inventory.android.view;

import android.os.*;
import android.support.v4.view.*;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.inventory.android.R;

public class UndobarController {
	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	private static final long HIDE_DELAY = 5000;

	// View
	private View mBarView;
	private TextView mMessageView;
	private ProgressBar mProgress;

	// Events
	private UndoListener mUndoListener;
	private ViewPropertyAnimatorCompat mBarAnimator;
	private CountDownTimer cdt = new CountDownTimer(HIDE_DELAY, HIDE_DELAY / 100) {
		public void onTick(long millisUntilFinished) {
			float percent = millisUntilFinished / (float)HIDE_DELAY;
			mProgress.setProgress((int)(percent * 100));
		}

		public void onFinish() {
			hideUndoBar(false);
		}
	};

	// State objects
	private Parcelable mUndoToken;
	private CharSequence mUndoMessage;

	public UndobarController(View undoBarView, UndoListener undoListener) {
		mBarView = undoBarView;
		mBarAnimator = ViewCompat.animate(mBarView);
		mUndoListener = undoListener;

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

	public void showUndoBar(boolean immediate, CharSequence message, Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		cdt.cancel();
		cdt.start();

		mBarView.setVisibility(View.VISIBLE);
		if (immediate) {
			ViewCompat.setAlpha(mBarView, 1);
		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(1)
					.setDuration(mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime))
					.setListener(null);
		}
	}

	public void hideUndoBar(boolean immediate) {
		cdt.cancel();
		if (immediate) {
			mBarView.setVisibility(View.GONE);
			ViewCompat.setAlpha(mBarView, 0);
			mUndoMessage = null;
			mUndoToken = null;
		} else {
			mBarAnimator.cancel();
			mBarAnimator
					.alpha(0)
					.setDuration(mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime))
					.setListener(new ViewPropertyAnimatorListenerAdapter() {
						public void onAnimationEnd(View view) {
							view.setVisibility(View.GONE);
							mUndoMessage = null;
							mUndoToken = null;
						}
					});
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence("undo_message", mUndoMessage);
		outState.putParcelable("undo_token", mUndoToken);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mUndoMessage = savedInstanceState.getCharSequence("undo_message");
			mUndoToken = savedInstanceState.getParcelable("undo_token");

			if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
				showUndoBar(true, mUndoMessage, mUndoToken);
			}
		}
	}
}
