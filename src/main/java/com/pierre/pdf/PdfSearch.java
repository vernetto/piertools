package com.pierre.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfSearch {
    public static void main(String[] args) throws IOException {
        String folderPath = "D:/temp/ubsarchive";  // change this
        String searchString = "turo";           // change this

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files == null) {
            System.out.println("No PDF files found in folder.");
            return;
        }

        int count = 0;
        for (File file : files) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text.toLowerCase().contains(searchString.toLowerCase())) {
                    System.out.println(++count + " found in: " + file.getAbsolutePath());
                    //System.out.println(text);
                    System.out.println("\n\n");
                }
            } catch (Exception e) {
                System.err.println("Could not read " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
