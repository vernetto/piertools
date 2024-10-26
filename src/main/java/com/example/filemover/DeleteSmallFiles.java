package com.example.filemover;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeleteSmallFiles {

    public static void main(String[] args) {
        // Define the folder where the script will search for files
        String folderPath = "X:\\pierre\\";
        long sizeLimit = 120 * 1024; // 120KB in bytes

        // Start the process
        deleteSmallFiles(folderPath, sizeLimit);
    }

    public static void deleteSmallFiles(String folderPath, long sizeLimit) {
        Path folder = Paths.get(folderPath);

        // Ensure the folder exists and is a directory
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            System.out.println("ERROR The specified path is not a directory.");
            return;
        }

        // Call the method to recursively delete files smaller than the size limit
        deleteFilesInDirectory(folder.toFile(), sizeLimit);
    }

    private static void deleteFilesInDirectory(File directory, long sizeLimit) {
        File[] files = directory.listFiles();

        if (files == null) {
            return; // No files or not a directory
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    long fileSize = Files.size(file.toPath());
                    if (fileSize < sizeLimit) {
                        if (file.delete()) {
                            System.out.println("Deleted file: " + file.getAbsolutePath());
                        } else {
                            System.out.println("ERROR Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("ERROR Error processing file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {
                // Recur into the subdirectory
                deleteFilesInDirectory(file, sizeLimit);
            }
        }
    }
}
