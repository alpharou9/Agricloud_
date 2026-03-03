package tn.esprit.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class email {
    private final String myEmail = "ayman.alnsiri.1k@gmail.com";
    private final String myPassword = "tdhi rduz jfia wkle";

    public void sendEmailWithQR(String to, String subject, String userName, String title, String date, String loc, String qrUrl) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, myPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("YOUR_EMAIL"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            String html = "<div style='font-family: Arial; border: 1px solid #ddd; padding: 20px; max-width: 500px;'>" +
                    "<h2 style='color: #2e7d32;'>🌿 Registration Confirmed!</h2>" +
                    "<p><b>Attendee:</b> " + userName + "</p>" +
                    "<p><b>Event:</b> " + title + "</p>" +
                    "<hr><p>Scan to open the farm location in Maps:</p>" +
                    "<img src='" + qrUrl + "' width='180' height='180' />" +
                    "<hr><p><b>Date:</b> " + date + "<br><b>Location:</b> " + loc + "</p></div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }
}