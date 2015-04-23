package net.twisterrob.inventory.android.activity.data;

import org.slf4j.*;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.*;
import android.text.TextUtils;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import static android.view.ViewGroup.LayoutParams.*;

import net.twisterrob.android.utils.concurrent.Callback;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<C extends BaseFragment<?>> extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDetailActivity.class);

	protected boolean wantDrawer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarSubtitle(getTitle());
		setActionBarTitle("...");

		super.setContentView(wantDrawer? R.layout.generic_activity_drawer : R.layout.generic_activity_nodrawer);

		String extrasError = checkExtras();
		if (extrasError != null) {
			App.toast(extrasError);
			finish();
			return;
		}

		if (savedInstanceState == null) {
			C fragment = onCreateFragment(null);
			if (fragment != null) {
				updateFragment(fragment).commit();
			}
		}
	}

	protected void setupTitleEditor() {
		new TitleEditor(this, new Callback<String>() {
			@Override public void call(String param) {
				if (TextUtils.getTrimmedLength(param) != 0) {
					try {
						updateName(param);
						getFragment().refresh();
					} catch (RuntimeException ex) {
						LOG.warn("Cannot set name to '{}'", param, ex);
						App.toastUser("Cannot set name to '" + param + "'");
					}
				}
			}
		}).install();
	}

	protected void updateName(String newName) {
		throw new UnsupportedOperationException("Cannot update name in " + this);
	}

	protected abstract C onCreateFragment(Bundle savedInstanceState);

	protected FragmentTransaction updateFragment(C fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (fragment != null) {
			ft.replace(R.id.activityRoot, fragment);
		}
		return ft;
	}

	protected String checkExtras() {
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getFragment().refresh();
	}

	@SuppressWarnings("unchecked")
	public C getFragment() {
		return (C)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}

	private static class TitleEditor implements OnClickListener, OnEditorActionListener, OnFocusChangeListener {
		private final ActionBarActivity activity;
		private final Callback<String> listener;
		private final EditText editor;

		public TitleEditor(ActionBarActivity activity, Callback<String> listener) {
			this.activity = activity;
			this.listener = listener;
			editor = new EditText(activity);
			editor.setSingleLine(true);
			editor.setImeOptions(EditorInfo.IME_ACTION_DONE);
		}

		@Override public void onClick(View v) {
			editor.setText(activity.getSupportActionBar().getTitle());
			editor.setSelection(editor.getText().length());
			editor.post(new Runnable() {
				@Override public void run() {
					editor.requestFocus();
					showKeyboard();
				}
			});
			editor.setOnEditorActionListener(this);
			activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
			activity.getSupportActionBar()
			        .setCustomView(editor, new ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
		}

		@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				hideKeyboard();
				listener.call(editor.getText().toString());

				activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
				activity.getSupportActionBar().setCustomView(null);
			}
			return false; // we just intercepted, didn't handle it
		}

		@Override public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				hideKeyboard();
			}
		}

		private void showKeyboard() {
			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editor, 0); //InputMethodManager.SHOW_IMPLICIT
		}
		private void hideKeyboard() {
			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
		}

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
	}
}
