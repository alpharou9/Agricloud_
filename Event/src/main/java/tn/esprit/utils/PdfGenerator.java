package tn.esprit.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import tn.esprit.entities.Participation;

public class PdfGenerator {
    public static void generateTicket(Participation p, String title, String date, String loc, String qrUrl, String path) {
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(path));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("AgriCloud Event Ticket").setBold().setFontSize(22).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Participant: " + p.getFullName()));
            document.add(new Paragraph("Event: " + title));
            document.add(new Paragraph("Location: " + loc));
            document.add(new Paragraph("Date: " + date));

            //Download image with User-Agent to avoid 403 Forbidden errors
            URLConnection conn = new URL(qrUrl).openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) { buffer.write(data, 0, nRead); }

            Image qrImage = new Image(ImageDataFactory.create(buffer.toByteArray()));
            document.add(new Paragraph("\nScan for GPS:"));
            document.add(qrImage);

            document.close();
            System.out.println("✅ PDF generated at: " + path);
        } catch (Exception e) {
            System.err.println("❌ PDF Error: " + e.getMessage());
        }
    }
}