package com.pierre.filescanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileScannerApp {

    public static void main(String[] args) {
        String[] foldersToScan = new String[] {"D:\\temp"};
        List<ScanFileInfo> scanFileInfos = new ArrayList<>();
        for (String folderPath : foldersToScan) {
            scanFolder(Paths.get(folderPath), scanFileInfos);
        }

        saveToJsonFile(scanFileInfos);
    }

    private static void scanFolder(Path folderPath, List<ScanFileInfo> scanFileInfos) {
        try {
            Files.walk(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            scanFileInfos.add(getFileInfo(file));
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.err.println("Error processing file: " + file + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error scanning folder: " + folderPath + " - " + e.getMessage());
        }
    }

    private static ScanFileInfo getFileInfo(Path file) throws IOException, NoSuchAlgorithmException {
        ScanFileInfo scanFileInfo = new ScanFileInfo();
        scanFileInfo.setFullPath(file.toString());
        scanFileInfo.setSize(Files.size(file));
        scanFileInfo.setLastModified(Files.getLastModifiedTime(file).toMillis());
        scanFileInfo.setSha2Digest(calculateSHA256(file));
        return scanFileInfo;
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

    private static void saveToJsonFile(List<ScanFileInfo> scanFileInfos) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String fileName = "filedump_" + timestamp + ".json";
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(fileName), scanFileInfos);
            System.out.println("File dump saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving JSON file: " + e.getMessage());
        }
    }
}
