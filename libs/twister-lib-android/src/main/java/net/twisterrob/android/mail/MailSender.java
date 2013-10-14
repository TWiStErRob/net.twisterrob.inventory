package net.twisterrob.android.mail;
import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.net.ssl.SSLSocketFactory;

public class MailSender extends javax.mail.Authenticator {
	private String m_user = ""; // username 
	private String m_pass = ""; // password 

	private String[] m_to;
	private String m_from = ""; // email sent from 

	private int m_port = 465; // default smtp port 
	private int m_sport = 465; // default socketfactory port 

	private String m_host = "smtp.gmail.com"; // default smtp server 

	private String m_subject = ""; // email subject 
	private String m_body = ""; // email body 

	private boolean m_auth = true; // smtp authentication - default on 

	private boolean m_debuggable = false; // debug mode on or off - default off 

	private Multipart m_multipart = new MimeMultipart();

	public MailSender() {
		// There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
		MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public MailSender(String user, String pass) {
		this();

		m_user = user;
		m_pass = pass;
	}

	public boolean send() throws Exception {
		Properties props = m_setProperties();

		if (!m_user.equals("") && !m_pass.equals("") && m_to.length > 0 && !m_from.equals("") && !m_subject.equals("")
				&& !m_body.equals("")) {
			Session session = Session.getInstance(props, this);

			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(m_from));

			InternetAddress[] addressTo = new InternetAddress[m_to.length];
			for (int i = 0; i < m_to.length; i++) {
				addressTo[i] = new InternetAddress(m_to[i]);
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

			msg.setSubject(m_subject);
			msg.setSentDate(new Date());

			// setup message body 
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(m_body);
			m_multipart.addBodyPart(messageBodyPart);

			// Put parts in message 
			msg.setContent(m_multipart);

			// send email 
			Transport.send(msg);

			return true;
		}
		return false;
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		m_multipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(m_user, m_pass);
	}

	private Properties m_setProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", m_host);

		if (m_debuggable) {
			props.put("mail.debug", String.valueOf(true));
		}

		if (m_auth) {
			props.put("mail.smtp.auth", String.valueOf(true));
		}

		props.put("mail.smtp.port", String.valueOf(m_port));
		props.put("mail.smtp.socketFactory.port", String.valueOf(m_sport));
		props.put("mail.smtp.socketFactory.class", SSLSocketFactory.class.getName());
		props.put("mail.smtp.socketFactory.fallback", String.valueOf(false));

		return props;
	}

	// the getters and setters

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

}
