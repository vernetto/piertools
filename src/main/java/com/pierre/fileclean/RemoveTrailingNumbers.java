package com.pierre.fileclean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RemoveTrailingNumbers {

    // Regex: parola di lettere seguita da numeri -> rimuovi i numeri
    private static final String REGEX = "([A-Za-zÀ-ÖØ-öø-ÿ]+)\\d+";

    public static void processFile(String inputPath, String outputPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String processed = line.replaceAll(REGEX, "$1");
                writer.write(processed);
                writer.newLine();
            }

            System.out.println("File written to: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String inputFile = "D:\\pierre\\calibre\\Klemperer, Viktor\\Die Tagebucher (446)\\Die Tagebucher - Klemperer, Viktor.txt";
        String input = inputFile;
        String output = inputFile + ".cleaned";

        processFile(input, output);
    }
}
