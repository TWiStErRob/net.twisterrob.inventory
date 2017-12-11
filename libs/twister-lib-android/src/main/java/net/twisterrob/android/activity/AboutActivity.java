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
import android.provider.Settings;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
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

		AboutInfo aboutInfo = getAboutInfo();
		LOG.trace("About info: {}", aboutInfo);
		populateInfo(aboutInfo);
	}

	protected void populateInfo(final AboutInfo aboutInfo) {
		OnClickListener feedbackAction = new OnClickListener() {
			@Override public void onClick(View v) {
				onFeedback(aboutInfo);
			}
		};
		OnClickListener settingsAction = new OnClickListener() {
			@Override public void onClick(View v) {
				openInSettings(aboutInfo);
			}
		};
		OnClickListener playStoreAction = new OnClickListener() {
			@Override public void onClick(View v) {
				openInPlayStore(aboutInfo);
			}
		};

		TextView feedback = (TextView)findViewById(R.id.about_feedback);
		feedback.setOnClickListener(feedbackAction);

		TextView nameText = (TextView)findViewById(R.id.about_name);
		nameText.setText(aboutInfo.appLabel);
		nameText.setOnClickListener(playStoreAction);

		ImageView iconImage = (ImageView)findViewById(R.id.about_icon);
		iconImage.setImageDrawable(aboutInfo.appIcon);
		iconImage.setOnClickListener(playStoreAction);

		TextView versionText = (TextView)findViewById(R.id.about_version);
		versionText.setText(getString(R.string.about_version, aboutInfo.versionName));
		versionText.setOnClickListener(settingsAction);

		TextView versionCodeText = (TextView)findViewById(R.id.about_version_code);
		versionCodeText.setText(String.valueOf(aboutInfo.versionCode));
		versionCodeText.setOnClickListener(settingsAction);
		AndroidTools.displayedIf(versionCodeText, getResources().getBoolean(R.bool.in_test));

		TextView packageText = (TextView)findViewById(R.id.about_package);
		packageText.setText(aboutInfo.applicationId);
		packageText.setOnClickListener(settingsAction);

		initSection(R.id.about_faq, R.id.about_faq_title);
		initSection(R.id.about_help, R.id.about_help_title);
		initSection(R.id.about_tips, R.id.about_tips_title);
	}

	private void initSection(@IdRes int sectionContentID, @IdRes int sectionTitleID) {
		TextView contentView = (TextView)findViewById(sectionContentID);
		contentView.setMovementMethod(LinkMovementMethod.getInstance());
		AndroidTools.displayedIfHasText(contentView);
		View titleView = findViewById(sectionTitleID);
		titleView.setVisibility(contentView.getVisibility());
	}

	protected void onFeedback(AboutInfo aboutInfo) {
		try {
			startActivity(createFeedbackIntent(aboutInfo));
		} catch (ActivityNotFoundException ex) {
			LOG.warn("Cannot start feedback intent({})", aboutInfo, ex);
			Toast.makeText(this, getString(R.string.about_feedback_fail, aboutInfo.email), Toast.LENGTH_LONG).show();
		}
	}

	protected void openInSettings(AboutInfo aboutInfo) {
		try {
			startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					.setData(Uri.parse("package:" + aboutInfo.applicationId))
			);
		} catch (ActivityNotFoundException ex) {
			LOG.warn("Cannot open app info in Settings", ex);
		}
	}

	protected void openInPlayStore(AboutInfo aboutInfo) {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW)
					.setData(Uri.parse("market://details?id=" + aboutInfo.applicationId))
			);
		} catch (ActivityNotFoundException ex) {
			LOG.warn("Cannot open app info in Play Store", ex);
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
