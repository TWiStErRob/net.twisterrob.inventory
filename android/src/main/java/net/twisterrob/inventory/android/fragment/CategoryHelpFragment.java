package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.Locale;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.webkit.*;
import android.widget.Toast;

import androidx.annotation.*;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.*;

public class CategoryHelpFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryHelpFragment.class);
	private WebView web;

	public CategoryHelpFragment() {
		setDynamicResource(DYN_OptionsMenu, R.menu.category_help);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ThreadPolicy realPolicy = StrictMode.allowThreadDiskWrites();
		try { // in older versions of Android WebViewDatabase.getInstance creates/opens a DB
			web = new WebView(container.getContext());
		} finally {
			StrictMode.setThreadPolicy(realPolicy);
		}
		web.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		web.getSettings().setJavaScriptEnabled(true);
		//web.getSettings().setBuiltInZoomControls(true);
		WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
		web.setWebViewClient(new WebViewClient() {
			@Override public void onPageFinished(WebView view, String url) {
				long category = Intents.getCategory(getArguments());
				getArguments().remove(Extras.CATEGORY_ID); // run only once
				if (category != Category.ID_ADD) {
					String key = CategoryDTO.getCache(getContext()).getCategoryKey(category);
					LOG.trace("Navigating to {} ({})", key, category);
					// http://stackoverflow.com/a/12266640/253468
					String jumpToCategory = "document.location.hash = '" + key + "';";
					view.loadUrl("javascript:(function() { " + jumpToCategory + "})()");
					// view.evaluateJavascript was added in API 19, so it cannot be used
				}
			}
			@SuppressWarnings("deprecation") // cannot use API 23 version, app supports API 10
			@Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				LOG.warn("WebView error #{} loading {}:\n{}", errorCode, failingUrl, description);
			}
		});
		web.setWebChromeClient(new WebChromeClient() {
			@Override public boolean onConsoleMessage(@NonNull ConsoleMessage msg) {
				LOG.debug("[{}] {}@{}: {}", msg.messageLevel(), msg.sourceId(), msg.lineNumber(), msg.message());
				return false;
			}
		});
		return web;
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState == null) {
			AndroidTools.executePreferParallel(new SimpleSafeAsyncTask<Void, Float, String>() {
				@Override protected void onPreExecute() {
					web.loadData("Loading...", "text/html", null);
				}
				@Override protected String doInBackground(Void ignore) {
					return new CategoryHelpBuilder(getActivity()).buildHTML();
				}
				@Override protected void onResult(String result, Void ignore) {
					web.loadData(result, "text/html", null);
				}
				@Override protected void onError(@NonNull Exception ex, Void ignore) {
					App.toastUser(ex.toString());
					getActivity().finish();
				}
			});
		} else {
			web.restoreState(savedInstanceState);
		}
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		web.saveState(outState);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_open: {
				final Context context = getContext();
				new SimpleSafeAsyncTask<Void, Void, File>() {
					private File file;
					@Nullable @Override protected File doInBackground(@Nullable Void ignore) throws IOException {
						file = Paths.getShareFile(getContext(), "html");
						new CategoryHelpBuilder(context).export(file);
						return file;
					}
					@Override protected void onResult(@Nullable File file, Void ignore) {
						startActivity(new Intent(Intent.ACTION_VIEW)
								.setDataAndType(Paths.getShareUri(context, file), "text/html")
								.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
						);
					}
					@Override protected void onError(@NonNull Exception ex, Void ignore) {
						LOG.warn("Cannot save to {}", file, ex);
					}
				}.execute();
				return true;
			}
			case R.id.action_category_save: {
				final Context context = getContext();
				new SimpleSafeAsyncTask<Void, Void, File>() {
					private File file;
					@Nullable @Override protected File doInBackground(@Nullable Void aVoid) throws Exception {
						File downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
						String name = String.format(Locale.ROOT,
								"%s - %s.html", getString(R.string.app_name), getString(R.string.category_guide));
						file = new File(downloads, name);
						new CategoryHelpBuilder(context).export(file);
						return file;
					}
					@Override protected void onResult(@Nullable File file, Void aVoid) {
						Toast.makeText(context, "Exported to " + file, Toast.LENGTH_LONG).show();
					}
					@Override protected void onError(@NonNull Exception ex, Void aVoid) {
						LOG.warn("Cannot save to {}", file, ex);
					}
				}.execute();
				return true;
			}
			case R.id.action_category_feedback:
				MainActivity.startImproveCategories(getContext(), null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	public static CategoryHelpFragment newInstance(long categoryID) {
		CategoryHelpFragment fragment = new CategoryHelpFragment();
		fragment.setArguments(Intents.bundleFromCategory(categoryID));
		return fragment;
	}
}
