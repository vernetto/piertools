package com.pierre.foldertimeupdate;

import java.io.IOException;
import java.nio.file.*;
        import java.nio.file.attribute.FileTime;
import java.util.*;
        import java.util.stream.*;

public class CalibreFolderTimeUpdaterTwoLevels {

    public static void main(String[] args) {
        Path calibreRoot = Path.of("D:/pierre/calibre");

        try (Stream<Path> subfolders = Files.list(calibreRoot)) {
            subfolders
                    .filter(Files::isDirectory)
                    .forEach(CalibreFolderTimeUpdaterTwoLevels::processSubfolder);
        } catch (IOException e) {
            System.err.println("Failed to list folders under calibre: " + e.getMessage());
        }
    }

    private static void processSubfolder(Path subfolder) {
        List<FileTime> subSubfolderTimes = new ArrayList<>();

        try (Stream<Path> subSubfolders = Files.list(subfolder)) {
            subSubfolders
                    .filter(Files::isDirectory)
                    .forEach(subSubfolder -> {
                        FileTime latestFileTime = getLatestFileTimeInFolder(subSubfolder);
                        if (latestFileTime != null) {
                            try {
                                if (true) Files.setLastModifiedTime(subSubfolder, latestFileTime);
                                System.out.printf("Updated sub-subfolder: %s to %s%n", subSubfolder, latestFileTime);
                                subSubfolderTimes.add(latestFileTime);
                            } catch (IOException e) {
                                System.err.printf("Failed to update sub-subfolder: %s - %s%n", subSubfolder, e.getMessage());
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to list sub-subfolders under: " + subfolder + " - " + e.getMessage());
        }

        subSubfolderTimes.stream()
                .max(Comparator.naturalOrder())
                .ifPresent(latest -> {
                    try {
                        if (true) Files.setLastModifiedTime(subfolder, latest);
                        System.out.printf("Updated subfolder: %s to %s%n", subfolder, latest);
                    } catch (IOException e) {
                        System.err.printf("Failed to update subfolder: %s - %s%n", subfolder, e.getMessage());
                    }
                });
    }

    private static FileTime getLatestFileTimeInFolder(Path folder) {
        try (Stream<Path> files = Files.walk(folder, 1)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(CalibreFolderTimeUpdaterTwoLevels::getFileTimeSafe)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        } catch (IOException e) {
            System.err.printf("Failed to get files in: %s - %s%n", folder, e.getMessage());
            return null;
        }
    }

    private static FileTime getFileTimeSafe(Path file) {
        try {
            return Files.getLastModifiedTime(file);
        } catch (IOException e) {
            System.err.printf("Failed to read time for: %s - %s%n", file, e.getMessage());
            return null;
        }
    }
}
