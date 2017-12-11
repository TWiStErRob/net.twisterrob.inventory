package net.twisterrob.android.view;

import java.lang.ref.WeakReference;
import java.util.*;

import android.content.Context;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.*;

import net.twisterrob.android.utils.listeners.AnimationListenerAdapter;
import net.twisterrob.android.utils.tools.TextTools;
import net.twisterrob.java.collections.*;

/**
 * <pre><code>
 *     &lt;net.twisterrob.android.view.CommentsView
 *     		android:layout_width="match_parent" 
 *          android:layout_height="wrap_content"
 *          android:maxLines="1"
 *          android:ellipsize="end"
 *      /&gt;
 * </code></pre>
 */
// TODO make it nicer with something like android:animateLayoutChanges="true"
public class CommentsView extends AppCompatTextView {
	public interface Comment {
		CharSequence getAuthor();
		CharSequence getMessage();
	}

	private static final int MESSAGE_NEXT = 1;
	private static final int CHANGE_DELAY = 5000;
	private static final int ANIMATION_DURATION = 500;

	private @NonNull Iterator<Comment> commentProvider = EmptyIterator.get();
	private @Nullable Comment currentComment;

	private final Handler delayHandler = new DelayHandler(new WeakReference<>(this));

	private final AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
	private final AlphaAnimation fadeOut = new AlphaAnimation(1, 0);

	public CommentsView(Context context) {
		super(context);
		init();
	}
	public CommentsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public CommentsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setVisibility(GONE); // hide on first display

		fadeOut.setDuration(ANIMATION_DURATION / 2);
		fadeOut.setAnimationListener(new AnimationListenerAdapter() {
			@Override public void onAnimationEnd(Animation animation) {
				if (currentComment != null) {
					startAnimation(fadeIn);
				} else {
					bind(null);
					setVisibility(GONE);
				}
			}
		});
		fadeIn.setDuration(ANIMATION_DURATION / 2);
		fadeIn.setAnimationListener(new AnimationListenerAdapter() {
			@Override public void onAnimationStart(Animation animation) {
				setVisibility(VISIBLE);
				bind(currentComment);
			}
			@Override public void onAnimationEnd(Animation animation) {
				Message message = delayHandler.obtainMessage(MESSAGE_NEXT);
				delayHandler.sendMessageDelayed(message, CHANGE_DELAY);
			}
		});
	}

	public void setComment(@NonNull Comment comment) {
		setComments(new PerpetualIterator<>(Collections.singletonList(comment)));
	}
	public void setComments(@NonNull Iterable<Comment> comments) {
		setComments(new PerpetualIterator<>(comments));
	}
	public void setComments(@NonNull Iterator<Comment> commentProvider) {
		this.commentProvider = commentProvider;
		reset();
		nextComment();
	}

	private void nextComment() {
		if (!commentProvider.hasNext()) {
			noComment();
			return;
		}

		Comment nextComment = commentProvider.next();
		if (currentComment == nextComment) {
			singleComment();
		} else {
			currentComment = nextComment;
			transitionComment();
		}
	}

	/** Fades out, clears, then hides the view. */
	private void noComment() {
		startAnimation(fadeOut);
	}

	/** Binds, shows the view (if needed) and fades in (if needed). */
	private void singleComment() {
		if (getVisibility() != View.VISIBLE) {
			startAnimation(fadeIn);
		} else {
			bind(currentComment); // make sure we have the current displayed
			// don't change the view further
		}
	}

	/** Fades out (if needed), binds and fades in. */
	private void transitionComment() {
		if (getVisibility() != View.VISIBLE) {
			startAnimation(fadeIn); // binds and queues nextComment
		} else {
			startAnimation(fadeOut); // starts fadeIn after ended
		}
	}

	private void bind(Comment comment) {
		SpannableStringBuilder builder = null;
		if (comment != null) {
			builder = new SpannableStringBuilder();
			TextTools.appendBold(builder, comment.getAuthor());
			builder.append(":");
			TextTools.appendItalic(builder, comment.getMessage());
		}
		setText(builder);
	}

	private void reset() {
		delayHandler.removeCallbacksAndMessages(null);
		fadeOut.cancel();
		fadeIn.cancel();
		currentComment = null;
	}

	private static class DelayHandler extends Handler {
		private final WeakReference<CommentsView> outer;
		public DelayHandler(WeakReference<CommentsView> outer) {
			super(Looper.getMainLooper());
			this.outer = outer;
		}
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_NEXT:
					CommentsView view = outer.get();
					if (view != null) {
						view.nextComment();
					}
					break;
				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
}
