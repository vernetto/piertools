package com.pierre.foldersync;

import java.io.File;

public class DeleteGitFolders {
    public static void main(String[] args) {
        String rootPath = "N:\\pierre\\github"; // Root directory
        File rootDir = new File(rootPath);

        if (rootDir.exists() && rootDir.isDirectory()) {
            deleteGitFolders(rootDir);
            System.out.println("Deletion completed.");
        } else {
            System.out.println("Invalid directory: " + rootPath);
        }
    }

    private static void deleteGitFolders(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (".git".equals(file.getName())) {
                        deleteDirectory(file);
                        System.out.println("Deleted: " + file.getAbsolutePath());
                    } else {
                        deleteGitFolders(file); // Recursively check subdirectories
                    }
                }
            }
        }
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
