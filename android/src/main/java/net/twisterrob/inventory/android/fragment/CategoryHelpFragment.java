package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.webkit.*;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.*;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewAssetLoader.InternalStoragePathHandler;
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler;
import dagger.hilt.android.AndroidEntryPoint;
import kotlin.io.FilesKt;

import net.twisterrob.android.contracts.CreateOpenableDocument;
import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.categories.cache.CategoryCache;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.io.IOTools;

@AndroidEntryPoint
@DebugHelper
@SuppressLint("StaticFieldLeak") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/257
public class CategoryHelpFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryHelpFragment.class);
	private WebView web;

	@Inject protected CategoryCache cache;

	public CategoryHelpFragment() {
		setDynamicResource(DYN_OptionsMenu, R.menu.category_help);
	}

	private final @NonNull ActivityResultLauncher<String> saveAs = registerForActivityResult(
			new CreateOpenableDocument("text/html"),
			new ActivityResultCallback<Uri>() {
				@Override public void onActivityResult(@Nullable Uri result) {
					if (result != null) {
						doSaveAs(result);
					}
				}
			}
	);

	@SuppressLint("SetJavaScriptEnabled")
	@Override public @NonNull View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
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
			private final WebViewAssetLoader website = setupWebsite();

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				return website.shouldInterceptRequest(request.getUrl());
			}

			@Override public void onPageFinished(WebView view, String url) {
				if (url.endsWith("/categories/index.html")) {
					web.clearHistory(); // https://stackoverflow.com/a/8103581/253468
					long category = Intents.getCategory(requireArguments());
					requireArguments().remove(Extras.CATEGORY_ID); // run only once
					if (category != Category.ID_ADD) {
						String key = cache.getCategoryKey(category);
						LOG.trace("Navigating to {} ({})", key, category);
						// http://stackoverflow.com/a/12266640/253468
						String jumpToCategory = "document.location.hash = '" + key + "';";
						view.loadUrl("javascript:(function() { " + jumpToCategory + "})()");
						// view.evaluateJavascript was added in API 19, so it cannot be used
					}
				}
			}
			@SuppressWarnings("deprecation") // cannot use API 23 version, app supports API 21
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
		requireActivity()
				.getOnBackPressedDispatcher()
				.addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
					@Override public void handleOnBackPressed() {
						if (web.canGoBack()) {
							web.goBack();
						} else {
							remove();
							requireActivity().onBackPressed();
						}
					}
				});
		return web;
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState == null) {
			AndroidTools.executePreferParallel(new SimpleSafeAsyncTask<Void, Float, String>() {
				@Override protected void onPreExecute() {
					web.loadData("Loading...", "text/html", null);
				}
				@Override protected String doInBackground(Void ignore) throws IOException {
					File dir = new File(requireContext().getCacheDir(), "categories");
					IOTools.ensure(dir);
					File file = new File(dir, "index.html");
					String html = new CategoryHelpBuilder(requireContext(), true).buildHTML();
					IOTools.writeAll(new FileOutputStream(file), html);
					return FilesKt.relativeTo(file, requireContext().getCacheDir()).toString();
				}
				@Override protected void onResult(String result, Void ignore) {
					// See CategoryHelpFragment.setupWebsite.
					web.loadUrl("https://inventory.twisterrob.net/" + result);
				}
				@Override protected void onError(@NonNull Exception ex, Void ignore) {
					App.toastUser(ex.toString());
					requireActivity().finish();
				}
			});
		} else {
			web.restoreState(savedInstanceState);
		}
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		web.saveState(outState);
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_category_open) {
			final Context context = requireContext();
			AndroidTools.executePreferParallel(new SimpleSafeAsyncTask<Void, Void, File>() {
				private File file;
				@Override protected @Nullable File doInBackground(@Nullable Void ignore)
						throws IOException {
					file = Paths.getShareFile(context, "html");
					String html = new CategoryHelpBuilder(context, false).buildHTML();
					IOTools.writeAll(new FileOutputStream(file), html);
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
			});
			return true;
		} else if (id == R.id.action_category_save) {
			String name = String.format(Locale.ROOT, "%s - %s.html",
					getString(R.string.app_name), getString(R.string.category_guide));
			saveAs.launch(name);
			return true;
		} else if (id == R.id.action_category_feedback) {
			MainActivity.startImproveCategories(requireContext(), cache, null);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void doSaveAs(@NonNull Uri target) {
		final Context context = requireContext();
		AndroidTools.executePreferParallel(new SimpleSafeAsyncTask<Uri, Void, Void>() {
			@Override protected Void doInBackground(Uri target) throws Exception {
				String html = new CategoryHelpBuilder(context, false).buildHTML();
				IOTools.writeAll(context.getContentResolver().openOutputStream(target), html);
				return null;
			}
			@Override protected void onResult(@Nullable Void aVoid, Uri target) {
				Toast.makeText(context, "Exported to " + target, Toast.LENGTH_LONG).show();
			}
			@Override protected void onError(@NonNull Exception ex, Uri target) {
				LOG.warn("Cannot save to {}", target, ex);
			}
		}, target);
	}

	private @NonNull WebViewAssetLoader setupWebsite() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			File categories = new File(requireContext().getCacheDir(), "categories");
			return new WebViewAssetLoader.Builder()
					.setHttpAllowed(false) // https:// only.
					.setDomain("inventory.twisterrob.net")
					// Must be first, otherwise the the next one will just look on disk.
					// The suffix /res/ is important, otherwise the next one will never be used.
					.addPathHandler("/categories/res/", new ResourcesPathHandler(requireContext()))
					.addPathHandler("/categories/", new InternalStoragePathHandler(requireContext(), categories))
					.build();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}


	public static CategoryHelpFragment newInstance(long categoryID) {
		CategoryHelpFragment fragment = new CategoryHelpFragment();
		fragment.setArguments(Intents.bundleFromCategory(categoryID));
		return fragment;
	}
}
