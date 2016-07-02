package net.twisterrob.android.activity;

import java.util.Locale;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.*;

import static android.widget.ArrayAdapter.*;

import net.twisterrob.android.R;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.utils.ReflectionTools;

public class AboutActivity extends ListActivity {
	private static final Logger LOG = LoggerFactory.getLogger(AboutActivity.class);

	private CharSequence[] licenseContents;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		ListView list = getListView();
		list.addHeaderView(getLayoutInflater().inflate(R.layout.inc_about_header, list, false), null, false);
		list.addFooterView(getLayoutInflater().inflate(R.layout.inc_about_footer, list, false), null, false);

		setListAdapter(createFromResource(this, R.array.about_licenses, android.R.layout.simple_list_item_1));
		licenseContents = getResources().getTextArray(R.array.about_licenses_content);
		AndroidTools.displayedIf(findViewById(R.id.about_licenses_title), !getListAdapter().isEmpty());

		final AboutInfo aboutInfo = getAboutInfo();
		LOG.trace("About info: {}", aboutInfo);

		TextView feedback = (TextView)findViewById(R.id.about_feedback);
		feedback.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				onFeedback(aboutInfo);
			}
		});
		populateInfo(aboutInfo);
	}

	protected void populateInfo(AboutInfo aboutInfo) {
		TextView nameText = (TextView)findViewById(R.id.about_name);
		nameText.setText(aboutInfo.appLabel);
		nameText.setSelected(true); // hack to start marquee

		ImageView iconImage = (ImageView)findViewById(R.id.about_icon);
		iconImage.setImageDrawable(aboutInfo.appIcon);

		TextView versionText = (TextView)findViewById(R.id.about_version);
		versionText.setText(getString(R.string.about_version, aboutInfo.versionName));
		versionText.setSelected(true); // hack to start marquee

		TextView versionCodeText = (TextView)findViewById(R.id.about_version_code);
		versionCodeText.setText(String.valueOf(aboutInfo.versionCode));
		versionCodeText.setVisibility(getResources().getBoolean(R.bool.in_test)? View.VISIBLE : View.GONE);

		TextView packageText = (TextView)findViewById(R.id.about_package);
		packageText.setText(aboutInfo.applicationId);
		packageText.setSelected(true); // hack to start marquee

		TextView faq = (TextView)findViewById(R.id.about_faq);
		AndroidTools.displayedIfHasText(faq);
		findViewById(R.id.about_faq_title).setVisibility(faq.getVisibility());

		TextView help = (TextView)findViewById(R.id.about_help);
		AndroidTools.displayedIfHasText(help);
		findViewById(R.id.about_help_title).setVisibility(help.getVisibility());

		TextView tips = (TextView)findViewById(R.id.about_tips);
		AndroidTools.displayedIfHasText(tips);
		findViewById(R.id.about_tips_title).setVisibility(tips.getVisibility());
	}

	protected void onFeedback(AboutInfo aboutInfo) {
		try {
			startActivity(createFeedbackIntent(aboutInfo));
		} catch (ActivityNotFoundException ex) {
			LOG.warn("Cannot start feedback intent({})", aboutInfo, ex);
			Toast.makeText(this, getString(R.string.about_feedback_fail, aboutInfo.email), Toast.LENGTH_LONG).show();
		}
	}

	protected @NonNull Intent createFeedbackIntent(AboutInfo aboutInfo) {
		final Intent feedbackIntent = new Intent(Intent.ACTION_VIEW);
		feedbackIntent.setData(Uri.parse("mailto:" + aboutInfo.email));
		feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {aboutInfo.email});
		feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_feedback_subject,
				aboutInfo.appLabel));
		feedbackIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.about_feedback_body,
				aboutInfo.applicationId, aboutInfo.versionName));
		return feedbackIntent;
	}

	protected @NonNull AboutInfo getAboutInfo() {
		AboutInfo about = new AboutInfo();

		Context app = getApplicationContext();
		ApplicationInfo appInfo = app.getApplicationInfo();

		about.appLabel = getResources().getString(appInfo.labelRes);
		about.appIcon = appInfo.loadIcon(app.getPackageManager());
		about.applicationId = app.getPackageName();
		String buildConfigClass = app.getClass().getPackage().getName() + ".BuildConfig";
		about.email = ReflectionTools.getStatic(buildConfigClass, "EMAIL");
		if (about.email == null) {
			about.email = net.twisterrob.android.BuildConfig.EMAIL; // library's fallback
		}

		PackageInfo pkgInfo;
		try {
			pkgInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
			about.versionName = pkgInfo.versionName;
			about.versionCode = pkgInfo.versionCode;
		} catch (NameNotFoundException ex) {
			about.versionName = "?";
		}
		return about;
	}

	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		CharSequence currentItem = (CharSequence)l.getAdapter().getItem(position);
		CharSequence content = id < licenseContents.length? licenseContents[(int)id] : null;
		if (TextUtils.isEmpty(content) || TextUtils.getTrimmedLength(content) == 0) {
			content = getText(R.string.about_licenses_missing);
		}
		AlertDialog dialog = new Builder(this)
				.setTitle(currentItem)
				.setPositiveButton(android.R.string.ok, null)
				.setView(createLicenseContents(content))
				.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	protected View createLicenseContents(CharSequence content) {
		@SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_about_license, null);
		TextView message = (TextView)view.findViewById(android.R.id.message);
		message.setText(content);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}

	protected static class AboutInfo {
		protected String appLabel;
		protected Drawable appIcon;
		protected String versionName;
		protected int versionCode;
		protected String applicationId;
		protected String email;

		@Override public String toString() {
			return String.format(Locale.ROOT, "%s(%s): %s v%s(%d) -> %s",
					appLabel, appIcon, applicationId, versionName, versionCode, email);
		}
	}
}
