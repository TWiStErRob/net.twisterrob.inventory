package net.twisterrob.inventory.android.activity.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.annotation.PluralsRes;
import android.support.v7.app.ActionBar.LayoutParams;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import static android.view.ViewGroup.LayoutParams.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<F extends BaseFragment<?>> extends SingleFragmentActivity<F> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDetailActivity.class);

	private final @PluralsRes int typePlural;
	private TitleEditor titleEditor;

	protected BaseDetailActivity(@PluralsRes int typePlural) {
		this.typePlural = typePlural;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarSubtitle(getTitle());
		setActionBarTitle("...");
	}

	@SuppressWarnings("UnusedReturnValue") // best effort to do the editing in title, so it's safe to ignore
	protected boolean setupTitleEditor() {
		titleEditor = new TitleEditor(this, new TitleEditor.TitleEditListener() {
			@Override public void titleChange(String oldName, String newName) {
				if (TextUtils.getTrimmedLength(newName) == 0) {
					return;
				}
				try {
					updateName(newName);
					getFragment().refresh();
				} catch (Exception ex) {
					LOG.warn("Cannot set name from '{}' to '{}'", oldName, newName, ex);
					App.toastUser(App.getError(ex, R.string.action_rename_failed,
							getResources().getQuantityString(typePlural, 1), oldName, newName));
				}
			}
		});
		boolean installed = titleEditor.install();
		if (!installed) {
			titleEditor = null;
		}
		return installed;
	}

	@Override public void onBackPressed() {
		if (titleEditor != null && titleEditor.isActive()) {
			titleEditor.finish();
			return;
		}
		super.onBackPressed();
	}

	@Override public boolean onSupportNavigateUp() {
		if (titleEditor != null && titleEditor.isActive()) {
			titleEditor.finish();
			return true;
		}
		return super.onSupportNavigateUp();
	}

	protected void updateName(String newName) {
		throw new UnsupportedOperationException("Cannot update name in " + this);
	}

	private static class TitleEditor implements OnClickListener, OnEditorActionListener {
		private String oldName;

		public interface TitleEditListener {
			void titleChange(String oldName, String newName);
		}

		private final BaseActivity activity;
		private final TitleEditListener listener;
		private final EditText editor;

		public TitleEditor(BaseActivity activity, TitleEditListener listener) {
			this.activity = activity;
			this.listener = listener;
			editor = new EditText(activity);
			editor.setSingleLine(true);
			editor.setImeOptions(EditorInfo.IME_ACTION_DONE);
		}

		@Override public void onClick(View v) {
			CharSequence oldTitle = activity.getSupportActionBar().getTitle();
			oldName = oldTitle != null? oldTitle.toString() : null;
			start(oldName);
		}

		@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				String newName = editor.getText().toString();
				listener.titleChange(oldName, newName);

				finish();
			}
			return false; // we just intercepted, didn't handle it
		}

		public boolean isActive() {
			return activity.getSupportActionBar().getCustomView() == editor;
		}

		/**
		 * @return whether the it managed to install the action bar title listener hack
		 */
		public boolean install() {
			View actionBarTitle = AndroidTools.findActionBarTitle(activity.getWindow().getDecorView());
			if (actionBarTitle != null) {
				activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
				actionBarTitle.setOnClickListener(this);
				View actionBarSubtitle = AndroidTools.findActionBarSubTitle(activity.getWindow().getDecorView());
				if (actionBarSubtitle != null) {
					actionBarSubtitle.setOnClickListener(this);
				}
				return true;
			}
			return false;
		}

		public void start(CharSequence initial) {
			editor.setText(initial);
			editor.setSelection(editor.getText().length());
			editor.setOnEditorActionListener(this);
			AndroidTools.showKeyboard(editor);

			activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
			LayoutParams fillHorizontal = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
			activity.getSupportActionBar().setCustomView(editor, fillHorizontal);
		}

		public void finish() {
			AndroidTools.hideKeyboard(editor);
			activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
			activity.getSupportActionBar().setCustomView(null);
		}
	}
}
