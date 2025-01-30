
package com.pierre.foldersync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class FolderSync {
    public final static boolean DRYRUN = false;

    public static void main(String[] args) {
        Path source = Path.of("I:\\pierre\\pictures");
        Path target = Path.of("N:\\pierre\\pictures");

        try {
            syncFolders(source, target);
            System.out.println("Folder synchronization completed successfully.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void syncFolders(Path source, Path target) throws IOException {
        if (!Files.exists(target)) {
            if (!DRYRUN) Files.createDirectories(target);
            System.out.println("MKDIR " + target);
        }

        // Copy new and updated files from source to target
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path relativePath = source.relativize(sourcePath);
                Path targetPath = target.resolve(relativePath);

                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        if (!DRYRUN) Files.createDirectories(targetPath);
                        System.out.println("MKDIR " + targetPath);
                    }
                } else {
                    if (!Files.exists(targetPath) ||
                        Files.getLastModifiedTime(sourcePath).compareTo(Files.getLastModifiedTime(targetPath)) > 0) {
                        if (!DRYRUN) Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("COPY " + sourcePath + " " + targetPath);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error syncing files: " + e.getMessage(), e);
            }
        });

        // Delete files from target that are not in source
        Files.walk(target).forEach(targetPath -> {
            try {
                Path relativePath = target.relativize(targetPath);
                Path sourcePath = source.resolve(relativePath);

                if (!Files.exists(sourcePath)) {
                    if (!DRYRUN) {
                        if (Files.isDirectory(targetPath)) {
                            deleteDirectory(targetPath);
                        }
                        else {
                            Files.delete(targetPath);
                        }
                    }
                    System.out.println("DELETE " + targetPath);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error deleting files: " + e.getMessage(), e);
            }
        });
    }

    public static void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()) // Ensures files are deleted before their parent directories
                    .forEach(p -> {
                        try {
                            System.out.println("DELETING " + p);
                            Files.delete(p);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

}
