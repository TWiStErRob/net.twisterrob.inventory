package net.twisterrob.java.io;

import java.io.*;
import java.net.*;
import java.util.Locale;

import net.twisterrob.java.utils.ArrayTools;

public class MailSender {
	private String[] m_to;
	private String m_from = ""; // email sent from

	private String m_host = "http://twisterrob-london.appspot.com/InternalFeedback"; // default smtp server

	private String m_subject = ""; // email subject
	private String m_body = ""; // email body

	public void send() throws IOException {
		try {
			URL url = new URL(m_host);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.connect();
			IOTools.writeAll(conn.getOutputStream(), this.toString());
			InputStream response = new BufferedInputStream(conn.getInputStream());
			String result = IOTools.readAll(response, "UTF-8");
			conn.disconnect();
			if (result != null && result.trim().length() != 0) {
				throw new IOException("Server responded with: " + result);
			}
		} catch (IOException ex) {
			throw new IOException("Cannot send " + this, ex);
		}
	}

	public String getBody() {
		return m_body;
	}
	public void setBody(String body) {
		this.m_body = body;
	}

	public String[] getTo() {
		return m_to;
	}
	public void setTo(String... toArr) {
		this.m_to = toArr;
	}

	public String getFrom() {
		return m_from;
	}
	public void setFrom(String string) {
		this.m_from = string;
	}

	public String getSubject() {
		return m_subject;
	}
	public void setSubject(String string) {
		this.m_subject = string;
	}

	@Override public String toString() {
		return String.format(Locale.ROOT, "email from %s to %s: %s\n%s",
				getFrom(), ArrayTools.toString(getTo()), getSubject(), getBody());
	}
}
