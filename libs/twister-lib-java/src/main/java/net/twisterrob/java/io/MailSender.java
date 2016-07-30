package net.twisterrob.java.io;

import java.io.*;
import java.net.*;
import java.util.Locale;

import net.twisterrob.java.utils.ArrayTools;

public class MailSender {
	private String from = "";
	private String[] to;
	private String subject = "";
	private String body = "";

	public void send() throws IOException {
		try {
			URL url = new URL("http://twisterrob-london.appspot.com/InternalFeedback");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.connect();
			IOTools.writeAll(conn.getOutputStream(), this.toString());
			InputStream response = new BufferedInputStream(conn.getInputStream());
			String result = IOTools.readAll(response, "UTF-8");
			conn.disconnect();
			if (result.trim().length() != 0) {
				throw new IOException("Server responded with: " + result);
			}
		} catch (IOException ex) {
			throw new IOException("Cannot send " + this, ex);
		}
	}

	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	public String[] getTo() {
		return to;
	}
	public void setTo(String... toArr) {
		this.to = toArr;
	}

	public String getFrom() {
		return from;
	}
	public void setFrom(String string) {
		this.from = string;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String string) {
		this.subject = string;
	}

	@Override public String toString() {
		return String.format(Locale.ROOT, "email from %s to %s: %s\n%s",
				getFrom(), ArrayTools.toString(getTo()), getSubject(), getBody());
	}
}
