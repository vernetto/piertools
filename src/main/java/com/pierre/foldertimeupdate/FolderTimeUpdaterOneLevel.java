package com.pierre.foldertimeupdate;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.stream.Stream;

public class FolderTimeUpdaterOneLevel {

    public static void main(String[] args) {
        Path root = Path.of("D:/pierre/pictures");

        try (Stream<Path> subfolders = Files.walk(root, 1)) {
            subfolders
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(root))
                    .forEach(FolderTimeUpdaterOneLevel::updateFolderTime);
        } catch (IOException e) {
            System.err.println("Error walking directory tree: " + e.getMessage());
        }
    }

    private static void updateFolderTime(Path folder) {
        try (Stream<Path> files = Files.walk(folder)) {
            files.filter(Files::isRegularFile)
                    .map(FolderTimeUpdaterOneLevel::getFileTime)
                    .filter(fileTime -> fileTime != null)
                    .max(Comparator.naturalOrder())
                    .ifPresent(latestTime -> {
                        try {
                            if (true) Files.setLastModifiedTime(folder, latestTime);
                            System.out.printf("Updated folder: %s to %s%n", folder, latestTime);
                        } catch (IOException e) {
                            System.err.println("Failed to set time for folder: " + folder + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading files in folder: " + folder + " - " + e.getMessage());
        }
    }

    private static FileTime getFileTime(Path file) {
        try {
            return Files.getLastModifiedTime(file);
        } catch (IOException e) {
            System.err.println("Failed to get time for file: " + file + " - " + e.getMessage());
            return null;
        }
    }
}
