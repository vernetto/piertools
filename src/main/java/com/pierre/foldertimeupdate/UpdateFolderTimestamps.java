package com.pierre.foldertimeupdate;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class UpdateFolderTimestamps {

    public static void main(String[] args) throws IOException {
        Path rootPath = Paths.get("D:/pierre/github");

        try (Stream<Path> subfolders = Files.list(rootPath)) {
            subfolders.filter(Files::isDirectory)
                    .forEach(UpdateFolderTimestamps::updateFolderTimestamp);
        }
    }

    private static void updateFolderTimestamp(Path folder) {
        try (Stream<Path> files = Files.walk(folder)) {
            Optional<FileTime> latestFileTime = files.filter(Files::isRegularFile)
                    .map(UpdateFolderTimestamps::getLastModifiedTime)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(Comparator.naturalOrder());

            if (latestFileTime.isPresent()) {
                Files.setLastModifiedTime(folder, latestFileTime.get());
                System.out.println("Updated " + folder + " to " + latestFileTime.get());
            } else {
                System.out.println("No regular files found in " + folder + ". Skipping.");
            }
        } catch (IOException e) {
            System.err.println("Failed to process folder: " + folder);
            e.printStackTrace();
        }
    }

    private static Optional<FileTime> getLastModifiedTime(Path path) {
        try {
            return Optional.of(Files.getLastModifiedTime(path));
        } catch (IOException e) {
            System.err.println("Failed to get last modified time for: " + path);
            return Optional.empty();
        }
    }
}
