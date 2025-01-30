# youtubesubtitles



# **FileDiffCheckerApp: File Change Detection Tool**

## **Overview**
The **FileDiffCheckerApp** is a Java-based application designed to track changes in a specified directory by comparing the current state of files against a previously saved snapshot. The application identifies newly added, removed, and modified files and exports the results in a structured JSON format.

## **Key Features**
- **Directory Scanning**: Recursively scans a target folder (`D:\temp` by default) for all files.
- **File Change Detection**:
    - Identifies **added** files (not present in the previous scan).
    - Detects **removed** files (present in the previous scan but missing in the current scan).
    - Recognizes **modified** files based on file size, last modified time, and SHA-256 checksum.
- **Secure Hashing**: Uses **SHA-256** to verify file integrity and detect modifications.
- **JSON Output**: Saves detected changes in a structured JSON file (`filediffs_YYYYMMDDHHmm.json`).
- **Resilience**: Handles I/O errors and missing data gracefully to avoid program crashes.

## **Use Case Scenarios**
- **Backup and Synchronization Monitoring**: Identify unexpected file changes in backups.
- **Version Control for Files**: Track modifications in critical files or project directories.
- **Security Auditing**: Detect unauthorized file changes in monitored directories.

## **Technical Details**
- Developed in **Java** using **Java 17** conventions.
- Utilizes **Jackson** for JSON processing.
- Leverages **Java NIO** for file operations.
- Uses **SHA-256 hashing** for file integrity validation.

This tool is ideal for IT professionals, system administrators, and developers who need to track file changes efficiently. 🚀




# FolderSync

## Overview
FolderSync is a lightweight Java application designed to synchronize the contents of two folders. 
It ensures that the target folder mirrors the source folder by copying new or modified files and deleting obsolete files.

## Features
- **One-way synchronization**: Ensures that the target folder mirrors the source.
- **Dry-run mode**: Allows users to preview changes without making modifications.
- **File and directory creation**: Creates missing directories and copies new or updated files.
- **Deletion of obsolete files**: Removes files from the target folder that are no longer present in the source.
- **Efficient file comparison**: Uses timestamps to determine if a file needs to be updated.
- **Console logging**: Displays the synchronization process in the console.

## How It Works
1. **Scans the source folder** and checks for files and directories.
2. **Creates missing directories** in the target folder.
3. **Copies new and updated files** from the source to the target.
4. **Deletes files** in the target folder that no longer exist in the source.
5. **Logs all operations** (creation, copying, deletion) to the console.

## Usage
To run FolderSync, specify the source and target directories within the `main` method:

```java
Path source = Path.of("D:\\temp");
Path target = Path.of("D:\\temp2");
```

Then execute the application:
```sh
java -cp . com.pierre.foldersync.FolderSync
```

## Configuration
- **Dry-run mode**: By default, the application runs in dry-run mode (`DRYRUN = true`). Set it to `false` to enable actual file operations.

```java
public final static boolean DRYRUN = false;
```

## Dependencies
- Java 17 or later
- `java.nio.file` package for file operations

## Future Enhancements
- Add multi-threading for improved performance.
- Implement a GUI interface for user-friendly interaction.
- Support bi-directional synchronization.

## License
This project is licensed under the MIT License.

## Author
**Pierluigi Vernetto**

