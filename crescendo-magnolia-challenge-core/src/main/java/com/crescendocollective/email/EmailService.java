package com.crescendocollective.email;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private String username;
    private String password;
    private String smtpAuth;
    private String smtpStartlsOn;
    private String smtpHost;
    private String smtpPort;


    public EmailService(String username, String password, String smtpAuth, String smtpStartlsOn,
                 String smtpHost, String smtpPort) {
        this.username = username;
        this.password = password;
        this.smtpAuth = smtpAuth;
        this.smtpStartlsOn = smtpStartlsOn;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    public void sendEmail(String to, String from, String messagebody) {
        Properties emailSessionProps = new Properties();
        emailSessionProps.put("mail.smtp.auth", smtpAuth);
        emailSessionProps.put("mail.smtp.starttls.enable", smtpStartlsOn);
        emailSessionProps.put("mail.smtp.host", smtpHost);
        emailSessionProps.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(emailSessionProps,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Contact Request");
            message.setText(messagebody);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
