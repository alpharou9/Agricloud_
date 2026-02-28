package tn.esprit.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import tn.esprit.entities.Participation;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class PdfGenerator {

    public static void generateTicket(Participation p, String eventTitle, String eventDesc, String absolutePath) {
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(absolutePath));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // --- Header and Table logic remains the same ---
            document.add(new Paragraph("AgriCloud Event Ticket")
                    .setBold().setFontSize(24).setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GREEN));

            Table table = new Table(2);
            table.addCell("Attendee:");
            table.addCell(p.getFullName());
            table.addCell("Event:");
            table.addCell(eventTitle);
            document.add(table.setMarginTop(20).setHorizontalAlignment(HorizontalAlignment.CENTER));

            // --- UPDATED QR DATA ---
            // We now include the Description (Notes) in the QR content
            String qrData = "TICKET INFO\n" +
                    "Attendee: " + p.getFullName() + "\n" +
                    "Event: " + eventTitle + "\n" +
                    "Notes: " + eventDesc; // This is the description from your notes field

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Encoding the data into a 150x150 QR matrix
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 150, 150);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            Image qrImage = new Image(ImageDataFactory.create(pngOutputStream.toByteArray()));

            qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(new Paragraph("\n"));
            document.add(qrImage);

            document.add(new Paragraph("Scan to view event details and notes.")
                    .setItalic().setFontSize(10).setTextAlignment(TextAlignment.CENTER));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}