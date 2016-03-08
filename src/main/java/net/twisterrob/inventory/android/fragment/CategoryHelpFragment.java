package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.Locale;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.Build.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.webkit.*;
import android.widget.Toast;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.model.CategoryHelpBuilder;

public class CategoryHelpFragment extends BaseFragment {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryHelpFragment.class);

	public CategoryHelpFragment() {
		setDynamicResource(DYN_OptionsMenu, R.menu.category_help);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		WebView web = new WebView(container.getContext());
		web.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		web.getSettings().setJavaScriptEnabled(true);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
		}
		web.setWebViewClient(new WebViewClient() {
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

	@Override public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		WebView web = (WebView)view;
		web.loadData(new CategoryHelpBuilder(getActivity()).buildHTML(), "text/html", null);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_open: {
				Context context = getContext();
				File file = null;
				try {
					file = Paths.getShareFile(context, "html");
					new CategoryHelpBuilder(context).export(file);
					String authority = AndroidTools.findProviderAuthority(context, FileProvider.class).authority;
					Uri uri = FileProvider.getUriForFile(context, authority, file);
					startActivity(new Intent(Intent.ACTION_VIEW)
							.setDataAndType(uri, "text/html")
							.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
				} catch (IOException ex) {
					LOG.warn("Cannot save to {}", file, ex);
				}
				return true;
			}
			case R.id.action_category_save: {
				Context context = getContext();
				File file = null;
				try {
					File downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
					String name = String.format(Locale.ROOT,
							"%s - %s.html", getString(R.string.app_name), getString(R.string.category_guide));
					file = new File(downloads, name);
					new CategoryHelpBuilder(context).export(file);
					Toast.makeText(context, "Exported to " + file, Toast.LENGTH_LONG).show();
				} catch (IOException ex) {
					LOG.warn("Cannot save to {}", file, ex);
				}
				return true;
			}
			case R.id.action_category_feedback:
				startActivity(new Intent(Intent.ACTION_VIEW)
						.setData(Uri.parse("mailto:" + BuildConfig.EMAIL))
						.putExtra(Intent.EXTRA_TEXT, "How can we improve the Categories?")
						.putExtra(Intent.EXTRA_SUBJECT, "Magic Home Inventory Category Feedback"));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	public static CategoryHelpFragment newInstance() {
		return new CategoryHelpFragment();
	}
}
