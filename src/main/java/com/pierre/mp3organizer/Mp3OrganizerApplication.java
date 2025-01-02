package com.pierre.mp3organizer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;


public class Mp3OrganizerApplication  {

    public static void main(String[] args) throws IOException {
        Mp3OrganizerApplication mp3OrganizerApplication = new Mp3OrganizerApplication();
        mp3OrganizerApplication.run();
    }

    public void run() throws IOException {
        // Define the path to the directory containing MP3 files
        Path sourceDirectory = Paths.get("D:\\pierre\\audio\\");

        if (!Files.isDirectory(sourceDirectory)) {
            System.err.println("The provided path is not a directory.");
            return;
        }
        try (var filesStream = Files.list(sourceDirectory)) { // Use Files.list for top-level only
            var mp3Files = filesStream
                    .filter(Files::isRegularFile) // Exclude directories and process only regular files
                    .filter(file -> file.toString().endsWith(".mp3")) // Filter MP3 files
                    .filter(file -> isValidFileName(file.getFileName().toString())) // Validate file name
                    .collect(Collectors.toList());

            for (Path file : mp3Files) {
                String fileName = file.getFileName().toString();

                // Determine the folder name based on underscores
                String folderName = extractFolderName(fileName);

                if (folderName != null) {
                    // Create the target directory if it doesn't exist
                    Path targetDirectory = sourceDirectory.resolve(folderName);
                    if (!Files.exists(targetDirectory)) {
                        Files.createDirectory(targetDirectory);
                    }

                    // Move the file to the target directory
                    Path targetFile = targetDirectory.resolve(fileName);
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("Moved " + fileName + " to " + targetDirectory);
                } else {
                    System.out.println("Skipping file: " + fileName);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while organizing MP3 files: " + e.getMessage());
        }
    }

    /**
     * Validates the file name to ensure it contains at least one underscore.
     *
     * @param fileName The name of the file to validate.
     * @return true if the file name is valid; false otherwise.
     */
    private boolean isValidFileName(String fileName) {
        long underscoreCount = fileName.chars().filter(ch -> ch == '_').count();
        return underscoreCount == 1 || underscoreCount == 2;
    }

    /**
     * Extracts the folder name from the file name based on underscores.
     *
     * @param fileName The name of the file.
     * @return The folder name if valid, or null if the file does not match criteria.
     */
    private String extractFolderName(String fileName) {
        long underscoreCount = fileName.chars().filter(ch -> ch == '_').count();

        if (underscoreCount == 1) {
            // Folder name is everything before the first underscore
            return fileName.substring(0, fileName.indexOf('_'));
        } else if (underscoreCount == 2) {
            // Folder name is everything before the second underscore
            int firstUnderscore = fileName.indexOf('_');
            int secondUnderscore = fileName.indexOf('_', firstUnderscore + 1);
            return fileName.substring(0, secondUnderscore);
        }
        return null;
    }
}
