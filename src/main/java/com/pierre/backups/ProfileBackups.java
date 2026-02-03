package com.pierre.backups;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.zip.*;

public class ProfileBackups {

    private static final String DATE = LocalDate.now().toString();

    public static void main(String[] args) throws Exception {

        Path home = Path.of("C:\\Users\\verne\\AppData\\");

        /* =======================
           ADJUST PATHS IF NEEDED
           ======================= */

        Path firefoxLocal  = home.resolve("Local\\Mozilla\\Firefox\\Profiles\\up1ujaez.pvprofileFromAcer1-1742324018069");
        Path firefoxRemote = home.resolve("Roaming\\Mozilla\\Firefox\\Profiles\\up1ujaez.pvprofileFromAcer1-1742324018069"); // optional

        Path chromeProfile = home.resolve("Local\\Google\\Chrome\\User Data");

        Path iphoneRoot = home.resolve("Roaming\\Apple Computer\\MobileSync\\Backup");

        Path outputDir = Path.of("D:\\pierre\\downloads");

        /* =======================
           FIREFOX ZIP
           ======================= */
        zipIfExists(
                outputDir.resolve("firefox-backup-local-" + DATE + ".zip"),
                firefoxLocal
        );
        zipIfExists(
                outputDir.resolve("firefox-backup-remote-" + DATE + ".zip"),
                firefoxRemote
        );
        /* =======================
           CHROME ZIP
           ======================= */
        zipIfExists(
                outputDir.resolve("chrome-backup-" + DATE + ".zip"),
                chromeProfile
        );

        /* =======================
           IPHONE ZIP (latest only)
           ======================= */
        Path latestIphoneBackup = findLatestSubfolder(iphoneRoot);
        if (latestIphoneBackup != null) {
            zipSingleFolder(
                    outputDir.resolve("iphone-backup-" + DATE + ".zip"),
                    latestIphoneBackup
            );
        } else {
            System.out.println("⚠ No iPhone backup found.");
        }

        System.out.println("✅ All backups completed.");
    }

    /* ===================================================== */

    private static void zipIfExists(Path zipFile, Path... folders) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            for (Path folder : folders) {
                if (folder != null && Files.exists(folder)) {
                    zipFolder(folder, folder.getFileName().toString(), zos);
                }
            }
        }
        System.out.println("✔ Created " + zipFile.getFileName());
    }

    private static void zipSingleFolder(Path zipFile, Path folder) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            zipFolder(folder, folder.getFileName().toString(), zos);
        }
        System.out.println("✔ Created " + zipFile.getFileName());
    }

    private static void zipFolder(Path source, String prefix, ZipOutputStream zos) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String entry = prefix + "/" + source.relativize(file).toString().replace("\\", "/");
                zos.putNextEntry(new ZipEntry(entry));
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Path findLatestSubfolder(Path root) throws IOException {
        if (!Files.exists(root)) return null;

        Path latest = null;
        FileTime lastTime = FileTime.fromMillis(0);

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
            for (Path p : ds) {
                if (Files.isDirectory(p)) {
                    FileTime t = Files.getLastModifiedTime(p);
                    if (t.compareTo(lastTime) > 0) {
                        lastTime = t;
                        latest = p;
                    }
                }
            }
        }
        return latest;
    }
}
