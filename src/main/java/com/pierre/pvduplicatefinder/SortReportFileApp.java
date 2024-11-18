package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SortReportFileApp {


    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the entire file as a List<List<FileInfo>>
        List<List<FileInfo>> fileInfoLists = objectMapper.readValue(new File("report20241118.txt"),
                new TypeReference<ArrayList<List<FileInfo>>>() {});

        // Sort the outer list
        fileInfoLists.sort((list1, list2) -> {
            // Ensure both lists are non-empty before comparing
            if (list1.isEmpty() || list2.isEmpty()) {
                return 0; // Consider empty lists equal for sorting
            }
            // Compare the paths of the first FileInfo items as strings
            return list1.get(0).getPath().toString().compareTo(list2.get(0).getPath().toString());
        });

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), fileInfoLists);

    }

}
