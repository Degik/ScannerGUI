import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;


public class SendMail {

	private String destinatario;
	private String subject;
	private String messaggioText;
	private String username;
	private String password;
	
	public SendMail(String destinatario, String username, String subject, String messaggio, String password) {
		this.destinatario = destinatario;
		this.username = username;
		this.subject = subject;
		this.messaggioText = messaggio;
		this.password = password;
	}
	
	public void sendMail() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		
		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
		});
		
		try {
			MimeMessage messaggio = new MimeMessage(session);
			messaggio.setFrom(new InternetAddress(username));
			messaggio.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
			messaggio.setSubject(subject);
			messaggio.setText(messaggioText);
			Transport.send(messaggio);
			//System.out.println("Messaggio mandato a " + destinatario);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
}
