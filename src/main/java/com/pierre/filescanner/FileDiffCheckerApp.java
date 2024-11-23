package com.pierre.filescanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pierre.pvduplicatefinder.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileDiffCheckerApp {

    public static void main(String[] args) {
        Path folderPath = Paths.get("D:\\temp");
        String previousJsonFile = "filedump_202411231257.json";
        List<FileInfo> addedFiles = new ArrayList<>();
        List<FileInfo> removedFiles = new ArrayList<>();
        List<FileInfo> modifiedFiles = new ArrayList<>();

        try {
            List<FileInfo> previousFileInfos = readPreviousScanFileInfos(previousJsonFile);
            Map<String, FileInfo> previousScanFileInfoMap = new HashMap<>();
            for (FileInfo FileInfo : previousFileInfos) {
                previousScanFileInfoMap.put(FileInfo.getPath().toString(), FileInfo);
            }

            Set<String> currentFilesSet = new HashSet<>();

            Files.walk(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            FileInfo currentFileInfo = getScanFileInfo(file);
                            currentFilesSet.add(currentFileInfo.getPath().toString());
                            FileInfo previousFileInfo = previousScanFileInfoMap.get(currentFileInfo.getPath().toString());

                            if (previousFileInfo == null) {
                                addedFiles.add(currentFileInfo);
                            } else if (isModified(currentFileInfo, previousFileInfo)) {
                                modifiedFiles.add(currentFileInfo);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.err.println("Error processing file: " + file + " - " + e.getMessage());
                        }
                    });

            // Identify removed files
            for (FileInfo previousFileInfo : previousFileInfos) {
                if (!currentFilesSet.contains(previousFileInfo.getPath())) {
                    removedFiles.add(previousFileInfo);
                }
            }

            saveToJsonFile(addedFiles, removedFiles, modifiedFiles);
        } catch (IOException e) {
            System.err.println("Error reading folder or previous JSON file: " + e.getMessage());
        }

    }


    private static boolean isModified(FileInfo current, FileInfo previous) throws IOException, NoSuchAlgorithmException {
        if (previous == null) return true; // New file
        if (current.getSize() != previous.getSize() || current.getTime() != previous.getTime()) {
            // Size or lastModified differs, check sha2 for confirmation
            return !current.getSha2().equals(previous.getSha2());
        }
        return false; // No modification detected
    }

    private static FileInfo getScanFileInfo(Path file) throws IOException, NoSuchAlgorithmException {
        FileInfo FileInfo = new FileInfo();
        FileInfo.setPath(file.toAbsolutePath());
        FileInfo.setSize(Files.size(file));
        FileInfo.setTime(Files.getLastModifiedTime(file));
        FileInfo.setSha2(calculateSHA256(file));
        return FileInfo;
    }

    private static String calculateSHA256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static List<FileInfo> readPreviousScanFileInfos(String previousJsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(previousJsonFile), new TypeReference<List<FileInfo>>() {});
    }

    private static void saveToJsonFile(List<FileInfo> addedFiles, List<FileInfo> removedFiles, List<FileInfo> modifiedFiles) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String fileName = "filediffs_" + timestamp + ".json";
        ObjectMapper mapper = new ObjectMapper();

        Map<String, List<FileInfo>> result = new HashMap<>();
        result.put("added", addedFiles);
        result.put("removed", removedFiles);
        result.put("modified", modifiedFiles);

        try {
            mapper.writeValue(new File(fileName), result);
            System.out.println("File diffs saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving JSON file: " + e.getMessage());
        }
    }



    private static FileInfo getFileInfo(Path file) throws IOException, NoSuchAlgorithmException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPath(file.toAbsolutePath());
        fileInfo.setSize(Files.size(file));
        fileInfo.setTime(Files.getLastModifiedTime(file));
        fileInfo.setSha2(calculateSHA256(file));
        return fileInfo;
    }


}
