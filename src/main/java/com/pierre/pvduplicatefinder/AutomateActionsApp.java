package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutomateActionsApp {


    public static final String DUPLICATES_JSON_FILE = "duplicates_202411190842.json";

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the entire file as a List<List<FileInfo>>
        List<List<FileInfo>> fileInfoListsFromJSON = objectMapper.readValue(new File(DUPLICATES_JSON_FILE),
                new TypeReference<ArrayList<List<FileInfo>>>() {});

        List<FileInfoList> fileInfoList = fileInfoListsFromJSON.stream()
                .map(FileInfoList::new) // Use the FileInfoList constructor directly
                .collect(Collectors.toList());

        fileInfoList.forEach(fileInfos -> {
            if (fileInfos.containsBothPaths("denhaag20040223", "koln040215")) {
                fileInfos.forEach(fileInfo -> {
                    if (fileInfo.getPath().toString().contains("denhaag20040223")) {
                        fileInfo.setFileAction(FileAction.DEL);
                        System.out.println("marking DEL on " + fileInfo.getPath().toString());
                    }
                });
            }
        });

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(DUPLICATES_JSON_FILE), fileInfoListsFromJSON);

    }


}
