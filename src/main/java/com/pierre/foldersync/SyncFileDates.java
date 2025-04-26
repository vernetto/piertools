package com.pierre.foldersync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class SyncFileDates {

    public static void main(String[] args) {
        Path sourceRoot = Paths.get("F:/pierre/");
        Path targetRoot = Paths.get("D:/pierre/");

        try {
            Files.walk(sourceRoot).forEach(sourcePath -> {
                try {
                    // Calcola il percorso relativo
                    Path relativePath = sourceRoot.relativize(sourcePath);
                    Path targetPath = targetRoot.resolve(relativePath);

                    if (Files.exists(targetPath)) {
                        copyDates(sourcePath, targetPath);
                        System.out.println("Date copiate: " + targetPath);
                    }
                } catch (Exception e) {
                    System.err.println("Errore con " + sourcePath + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyDates(Path sourcePath, Path targetPath) throws IOException {
        BasicFileAttributes sourceAttr = Files.readAttributes(sourcePath, BasicFileAttributes.class);

        FileTime creationTime = sourceAttr.creationTime();
        FileTime lastModifiedTime = sourceAttr.lastModifiedTime();
        FileTime lastAccessTime = sourceAttr.lastAccessTime();

        Files.setAttribute(targetPath, "basic:creationTime", creationTime);
        Files.setLastModifiedTime(targetPath, lastModifiedTime);
        Files.setAttribute(targetPath, "basic:lastAccessTime", lastAccessTime);
    }
}
