package esprit.farouk.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtils {
    // Gmail SMTP Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465"; // SSL port
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // TODO: Change this
    private static final String SENDER_PASSWORD = "your-app-password"; // TODO: Use Gmail App Password

    /**
     * Sends an email using Gmail SMTP
     *
     * @param recipientEmail Recipient's email address
     * @param subject Email subject
     * @param messageBody Email body (can be HTML)
     * @return true if sent successfully, false otherwise
     */
    public static boolean sendEmail(String recipientEmail, String subject, String messageBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(messageBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✓ Email sent successfully to: " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends password reset email with 6-digit code
     */
    public static boolean sendPasswordResetEmail(String recipientEmail, String resetCode) {
        String subject = "AgriCloud - Password Reset Code";
        String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                     "<h2 style='color: #2E7D32;'>Password Reset Request</h2>" +
                     "<p>You requested to reset your password. Use the code below:</p>" +
                     "<div style='background: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px;'>" +
                     "<h1 style='color: #2E7D32; letter-spacing: 5px;'>" + resetCode + "</h1>" +
                     "</div>" +
                     "<p>This code will expire in 15 minutes.</p>" +
                     "<p>If you didn't request this, please ignore this email.</p>" +
                     "<hr style='margin-top: 30px;'/>" +
                     "<p style='color: #666; font-size: 12px;'>AgriCloud - Smart Farm Management System</p>" +
                     "</body></html>";

        return sendEmail(recipientEmail, subject, body);
    }
}
