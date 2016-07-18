package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;

import net.twisterrob.android.utils.log.*;
import net.twisterrob.java.io.MailSender;

public class MailSenderAsyncTask extends AsyncTask<String, Void, Boolean> {
	private static final Log LOG = LogFactory.getLog(Tag.IO);

	private final MailSender m = new MailSender();

	public MailSenderAsyncTask(String subject, String from, String... to) {
		m.setTo(to);
		m.setFrom(from);
		m.setSubject(subject);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			m.setBody(params[0]);
			m.send();
			return true;
		} catch (Exception ex) {
			LOG.error("Cannot send %s.", ex, m);
			return false;
		}
	}
}
