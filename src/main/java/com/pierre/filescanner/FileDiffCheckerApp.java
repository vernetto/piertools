package com.pierre.filescanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.stream.Collectors;

public class FileDiffCheckerApp {

    public static void main(String[] args) {
        Path folderPath = Paths.get("D:\\temp");
        String previousJsonFile = "filedump_202411231257.json";
        List<ScanFileInfo> addedFiles = new ArrayList<>();
        List<ScanFileInfo> removedFiles = new ArrayList<>();
        List<ScanFileInfo> modifiedFiles = new ArrayList<>();

        try {
            List<ScanFileInfo> previousScanFileInfos = readPreviousScanFileInfos(previousJsonFile);
            Map<String, ScanFileInfo> previousScanFileInfoMap = new HashMap<>();
            for (ScanFileInfo ScanFileInfo : previousScanFileInfos) {
                previousScanFileInfoMap.put(ScanFileInfo.getFullPath(), ScanFileInfo);
            }

            Set<String> currentFilesSet = new HashSet<>();

            Files.walk(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            ScanFileInfo currentScanFileInfo = getScanFileInfo(file);
                            currentFilesSet.add(currentScanFileInfo.getFullPath());
                            ScanFileInfo previousScanFileInfo = previousScanFileInfoMap.get(currentScanFileInfo.getFullPath());

                            if (previousScanFileInfo == null) {
                                addedFiles.add(currentScanFileInfo);
                            } else if (isModified(currentScanFileInfo, previousScanFileInfo)) {
                                modifiedFiles.add(currentScanFileInfo);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.err.println("Error processing file: " + file + " - " + e.getMessage());
                        }
                    });

            // Identify removed files
            for (ScanFileInfo previousScanFileInfo : previousScanFileInfos) {
                if (!currentFilesSet.contains(previousScanFileInfo.getFullPath())) {
                    removedFiles.add(previousScanFileInfo);
                }
            }

            saveToJsonFile(addedFiles, removedFiles, modifiedFiles);
        } catch (IOException e) {
            System.err.println("Error reading folder or previous JSON file: " + e.getMessage());
        }

    }


    private static boolean isModified(ScanFileInfo current, ScanFileInfo previous) throws IOException, NoSuchAlgorithmException {
        if (previous == null) return true; // New file
        if (current.getSize() != previous.getSize() || current.getLastModified() != previous.getLastModified()) {
            // Size or lastModified differs, check sha2 for confirmation
            return !current.getSha2Digest().equals(previous.getSha2Digest());
        }
        return false; // No modification detected
    }

    private static ScanFileInfo getScanFileInfo(Path file) throws IOException, NoSuchAlgorithmException {
        ScanFileInfo ScanFileInfo = new ScanFileInfo();
        ScanFileInfo.setFullPath(file.toString());
        ScanFileInfo.setSize(Files.size(file));
        ScanFileInfo.setLastModified(Files.getLastModifiedTime(file).toMillis());
        ScanFileInfo.setSha2Digest(calculateSHA256(file));
        return ScanFileInfo;
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

    private static List<ScanFileInfo> readPreviousScanFileInfos(String previousJsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(previousJsonFile), new TypeReference<List<ScanFileInfo>>() {});
    }

    private static void saveToJsonFile(List<ScanFileInfo> addedFiles, List<ScanFileInfo> removedFiles, List<ScanFileInfo> modifiedFiles) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String fileName = "filediffs_" + timestamp + ".json";
        ObjectMapper mapper = new ObjectMapper();

        Map<String, List<ScanFileInfo>> result = new HashMap<>();
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



    private static ScanFileInfo getFileInfo(Path file) throws IOException, NoSuchAlgorithmException {
        ScanFileInfo fileInfo = new ScanFileInfo();
        fileInfo.setFullPath(file.toString());
        fileInfo.setSize(Files.size(file));
        fileInfo.setLastModified(Files.getLastModifiedTime(file).toMillis());
        fileInfo.setSha2Digest(calculateSHA256(file));
        return fileInfo;
    }


}
