/**
 * Created by stuartkuredjian on 7/25/15.
 */

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class NotificationHandler {

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final int SMTP_HOST_PORT = 465;
    private static final String SMTP_AUTH_USER = "sgkur04@gmail.com";
    private static final String SMTP_AUTH_PWD  = "Hyrenkosa1";

    public NotificationHandler (String[] recipients, String subject, String message) throws Exception{
        for (int i = 0; i < recipients.length; i++) {
            execute(recipients[i], subject, message);
        }

    }

    public void execute(String recipients, String subject, String aMessage) throws Exception{
        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", SMTP_HOST_NAME);
        props.put("mail.smtps.auth", "true");
        // props.put("mail.smtps.quitwait", "false");

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject(subject);
        message.setContent(aMessage, "text/plain");

        message.addRecipient(Message.RecipientType.TO,
                new InternetAddress(recipients));

        transport.connect
                (SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

        transport.sendMessage(message,
                message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }
}