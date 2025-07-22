package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExecuteActionsApp {

    static final boolean DRYRUN = false;

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the entire file as a List<List<FileInfo>>
        List<List<FileInfo>> fileInfoLists = objectMapper.readValue(new File("duplicates_202507192050.json"),
                new TypeReference<ArrayList<List<FileInfo>>>() {});

        List<FileInfo> fileInfoListsFlat = fileInfoLists.stream()
                .flatMap(List::stream) // Flatten each inner list
                .filter(fileInfo -> !fileInfo.fileAction.equals(FileAction.NOTHING))
                .collect(Collectors.toList()); // Collect into a single List<FileInfo>

        fileInfoListsFlat.forEach(fileInfo -> {
            String json = null;
            try {
                json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileInfo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //System.out.println(json);
        });

        fileInfoListsFlat.forEach(fileInfo -> {
            if (Files.isRegularFile(fileInfo.getPath())) {
                if (fileInfo.getFileAction().equals(FileAction.DEL)) {
                    System.out.println("deleting " + fileInfo.getPath());
                    try {
                        if (!DRYRUN) Files.deleteIfExists(fileInfo.getPath());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (fileInfo.getFileAction().equals(FileAction.DELFOLDER)) {
                    System.out.println("deleting folder " + fileInfo.getPath().getParent());
                    try {
                        deleteRecursively(fileInfo.getPath().getParent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else {
                System.out.println("not a regular file or not found: " + fileInfo.getPath());
            }

        });

    }


    public static void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            // Walk the file tree and delete children before deleting the parent
            Files.walk(path)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // Sort in reverse order
                    .forEach(p -> {
                        try {
                            if (!DRYRUN) Files.delete(p);
                            System.out.println("Deleted: " + p);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete: " + p, e);
                        }
                    });
        }
    }
}
