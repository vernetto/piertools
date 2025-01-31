package com.pierre.foldersync;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class ZeroSizeFileFinder {

    public static void main(String[] args) {
        Path rootPath = Path.of("I:\\pierre");
        if (!Files.isDirectory(rootPath)) {
            System.out.println("Error: Provided path is not a directory or does not exist.");
            System.exit(1);
        }

        findZeroSizeFiles(rootPath);
    }

    private static void findZeroSizeFiles(Path rootPath) {
        try (Stream<Path> files = Files.walk(rootPath)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.size(path) == 0;
                        } catch (IOException e) {
                            System.err.println("Error accessing file: " + path + " - " + e.getMessage());
                            return false;
                        }
                    })
                    .forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error walking the file tree: " + e.getMessage());
        }
    }
}
