package esprit.farouk.utils;

import esprit.farouk.models.Event;
import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailUtils {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String FROM_EMAIL = "farouknakkach@gmail.com";
    private static final String FROM_PASSWORD = "fsqmwfikwjmjeygj";

    public static void sendResetCode(String toEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("AgriCloud - Password Reset Code");
        message.setText("Hello,\n\nYour password reset code is: " + code +
                "\n\nThis code will expire shortly. If you did not request this, please ignore this email." +
                "\n\nAgriCloud Team");

        Transport.send(message);
    }

    public static void sendOrderConfirmation(String toEmail, String customerName, long orderId, double totalAmount) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("AgriCloud - Order Confirmation #" + orderId);

        String emailBody = "Dear " + customerName + ",\n\n" +
                "Thank you for your order on AgriCloud!\n\n" +
                "Order Details:\n" +
                "Order Number: #" + orderId + "\n" +
                "Total Amount: $" + String.format("%.2f", totalAmount) + "\n\n" +
                "Your order has been confirmed and will be delivered to you within 3 business days maximum.\n\n" +
                "We will keep you updated on the status of your order.\n\n" +
                "Thank you for choosing AgriCloud!\n\n" +
                "Best regards,\n" +
                "The AgriCloud Team";

        message.setText(emailBody);
        Transport.send(message);
    }

    public static void sendEventTicket(String toEmail, String userName, Event event, long participationId) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy  HH:mm");
        DateTimeFormatter icalFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String dateStr = event.getEventDate() != null ? event.getEventDate().format(fmt) : "TBD";
        String endStr = event.getEndDate() != null ? " – " + event.getEndDate().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        String category = event.getCategory() != null ? event.getCategory() : "General";

        // iCalendar QR content — phones display it as a native event page (Add to Calendar)
        String dtStart = event.getEventDate() != null ? event.getEventDate().format(icalFmt) : "19700101T000000";
        String dtEnd   = event.getEndDate()   != null ? event.getEndDate().format(icalFmt)
                       : event.getEventDate() != null ? event.getEventDate().plusHours(2).format(icalFmt)
                       : "19700101T020000";
        String qrContent = "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//AgriCloud//AgriCloud//EN\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:" + participationId + "@agricloud.com\r\n" +
                "SUMMARY:" + event.getTitle() + "\r\n" +
                "DTSTART:" + dtStart + "\r\n" +
                "DTEND:" + dtEnd + "\r\n" +
                "LOCATION:" + event.getLocation() + "\r\n" +
                "DESCRIPTION:Participant: " + userName + "\\nRegistration #" + participationId + "\\nCategory: " + category + "\\nStatus: CONFIRMED\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR";

        byte[] qrBytes = QRCodeUtils.generateQRCodeBytes(qrContent, 300, 300);

        // HTML body
        String html = "<div style='font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #ddd;border-radius:8px;overflow:hidden'>" +
                "<div style='background:#2e7d32;padding:20px;text-align:center'>" +
                "<h1 style='color:#fff;margin:0;font-size:22px'>AgriCloud</h1>" +
                "<p style='color:#c8e6c9;margin:4px 0 0'>Event Registration Ticket</p>" +
                "</div>" +
                "<div style='padding:24px'>" +
                "<h2 style='color:#2e7d32;margin-top:0'>" + event.getTitle() + "</h2>" +
                "<table style='width:100%;border-collapse:collapse;font-size:14px'>" +
                "<tr><td style='padding:6px 0;color:#777;width:110px'>Date</td><td style='padding:6px 0;font-weight:bold'>" + dateStr + endStr + "</td></tr>" +
                "<tr><td style='padding:6px 0;color:#777'>Location</td><td style='padding:6px 0;font-weight:bold'>" + event.getLocation() + "</td></tr>" +
                "<tr><td style='padding:6px 0;color:#777'>Category</td><td style='padding:6px 0'>" + category + "</td></tr>" +
                "<tr><td style='padding:6px 0;color:#777'>Participant</td><td style='padding:6px 0'>" + userName + "</td></tr>" +
                "<tr><td style='padding:6px 0;color:#777'>Registration</td><td style='padding:6px 0'>#" + participationId + "</td></tr>" +
                "<tr><td style='padding:6px 0;color:#777'>Status</td><td style='padding:6px 0'><span style='background:#e8f5e9;color:#2e7d32;padding:2px 10px;border-radius:12px;font-weight:bold'>CONFIRMED</span></td></tr>" +
                "</table>" +
                "<div style='text-align:center;margin:24px 0 8px'>" +
                "<p style='color:#555;font-size:13px;margin-bottom:8px'>Present this QR code at the event entrance</p>" +
                "<img src='cid:qrcode' style='width:200px;height:200px'/>" +
                "</div>" +
                "</div>" +
                "<div style='background:#f5f5f5;padding:12px;text-align:center;font-size:12px;color:#999'>" +
                "AgriCloud — Smart Farm Management &nbsp;|&nbsp; This ticket is non-transferable" +
                "</div>" +
                "</div>";

        // Build multipart message
        MimeMultipart multipart = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=utf-8");
        multipart.addBodyPart(htmlPart);

        MimeBodyPart imagePart = new MimeBodyPart();
        imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(qrBytes, "image/png")));
        imagePart.setHeader("Content-ID", "<qrcode>");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("AgriCloud - Your Ticket for: " + event.getTitle());
        message.setContent(multipart);

        Transport.send(message);
    }
}
