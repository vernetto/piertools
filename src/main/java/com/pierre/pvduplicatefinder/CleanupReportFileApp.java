package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CleanupReportFileApp {


    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the entire file as a List<List<FileInfo>>
        List<List<FileInfo>> fileInfoLists = objectMapper.readValue(new File("report001.log"),
                new TypeReference<ArrayList<List<FileInfo>>>() {});

        String filterString = "iTunes";

        List<List<FileInfo>> listToSave = fileInfoLists.stream()
                // Filter each List<FileInfo> where none of the FileInfo paths contain "pippo"
                .filter(fileInfoList -> fileInfoList.stream()
                        .noneMatch(fileInfo -> fileInfo.getPath().toString().contains(filterString)))
                .collect(Collectors.toList());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), listToSave);

    }

}
