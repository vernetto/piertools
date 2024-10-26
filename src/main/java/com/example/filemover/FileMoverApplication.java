package com.example.filemover;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class FileMoverApplication implements CommandLineRunner {

    @Value("${source.directory}")
    private String sourceDirectory;

    @Value("${max.files.per.folder}")
    private int maxFilesPerFolder;

    public static void main(String[] args) {
        SpringApplication.run(FileMoverApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            moveFiles(sourceDirectory, maxFilesPerFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moveFiles(String sourceDirectory, int maxFilesPerFolder) throws IOException {
        Path sourcePath = Paths.get(sourceDirectory);

        if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("Source directory does not exist or is not a directory");
        }

        // List all files in the source directory
        List<Path> files = Files.list(sourcePath)
                .filter(Files::isRegularFile)  // Filter regular files
                .collect(Collectors.toList());

        int subfolderIndex = 1;
        int fileCount = 0;
        Path subfolderPath = createNewSubfolder(sourcePath, subfolderIndex);

        for (Path file : files) {
            // Move the file to the current subfolder
            System.out.println("moving " + file.getFileName());
            Files.move(file, subfolderPath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);

            fileCount++;

            // If the file count reaches the maximum, reset the count and create a new subfolder
            if (fileCount >= maxFilesPerFolder) {
                subfolderIndex++;
                subfolderPath = createNewSubfolder(sourcePath, subfolderIndex);
                fileCount = 0;
            }
        }

        System.out.println("File moving completed.");
    }

    private Path createNewSubfolder(Path sourcePath, int subfolderIndex) throws IOException {
        Path subfolderPath = sourcePath.resolve("Folder" + subfolderIndex);
        if (!Files.exists(subfolderPath)) {
            System.out.println("creating " + subfolderPath);
            Files.createDirectory(subfolderPath);
        }
        return subfolderPath;
    }
}
