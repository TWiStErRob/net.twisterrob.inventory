package net.twisterrob.android.mail;

import javax.mail.*;

import net.twisterrob.android.utils.log.*;
import android.os.AsyncTask;

public class MailSenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private static final Log LOG = LogFactory.getLog(Tag.IO);
	private static String s_username;
	private static String s_password;

	private final MailSender m = new MailSender(s_username, s_password);

	public MailSenderAsyncTask(String subject, String from, String... to) {
		m.setTo(to);
		m.setFrom(from);
		m.setSubject(subject);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			m.send();
			return true;
		} catch (AuthenticationFailedException ex) {
			LOG.error("Cannot use e-mail account.", ex);
			return false;
		} catch (MessagingException ex) {
			LOG.error("Sending mail to %s failed.", ex, (Object)m.getTo());
			return false;
		} catch (Exception ex) {
			LOG.error("Cannot send email.", ex);
			return false;
		}
	}

	public static void setUsername(String username) {
		s_username = username;
	}
	public static void setPassword(String password) {
		s_password = password;
	}
	public void setBody(String body) {
		m.setBody(body);
	}
}
