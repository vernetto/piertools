package com.pierre.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImagesToPdf {
    public static void main(String[] args) {
        String folderPath = "D:\\pierre\\downloads\\az305\\testpictures02";
        String outputPdf = "testpictures2.pdf";

        File folder = new File(folderPath);
        File[] imageFiles = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".bmp");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in " + folderPath);
            return;
        }

        // Sort by name to keep order
        Arrays.sort(imageFiles);

        try (PDDocument doc = new PDDocument()) {
            for (File imgFile : imageFiles) {
                PDImageXObject pdImage = PDImageXObject.createFromFile(imgFile.getAbsolutePath(), doc);

                PDRectangle rect = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                PDPage page = new PDPage(rect);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.drawImage(pdImage, 0, 0, rect.getWidth(), rect.getHeight());
                }

                System.out.println("Added: " + imgFile.getName());
            }

            doc.save(folderPath + "\\" + outputPdf);
            System.out.println("PDF created: " + outputPdf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
