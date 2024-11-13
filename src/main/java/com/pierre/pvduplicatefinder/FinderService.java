package com.pierre.pvduplicatefinder;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinderService {
    @Autowired
    FilesCrawler filesCrawler;
    BufferedWriter writer = null;

    public List<DuplicateCollection> findDuplicates(String... rootLocations) throws IOException {
        writer = new BufferedWriter(new FileWriter("report.log", true));
        List<DuplicateCollection> result = new ArrayList<>();
        List<FileInfo> allFiles = new ArrayList<>();
        for (String rootLocation : rootLocations)  {
            allFiles.addAll(filesCrawler.findAllFileInfo(rootLocation));
        }
        Collections.sort(allFiles, Comparator.comparing(FileInfo::getSize));
        writeData("printing all files, smallest first:");
        allFiles.forEach(fileInfo -> writeData(fileInfo.toString()));
        writeData("=====================");
        // now searching duplicates: group files by size first
        Map<Long, List<FileInfo>> allFilesGroupedBySize = allFiles.stream().collect(Collectors.groupingBy(FileInfo::getSize));
        allFilesGroupedBySize.forEach((aLong, fileInfos) -> writeData(aLong + " " + fileInfos));
        writeData("=====================");
        allFilesGroupedBySize.forEach((aLong, fileInfos) -> {
            if (aLong > 1) {
                fileInfos.forEach(fileInfo -> {
                    try {
                        fileInfo.sha2 = DigestUtils.sha256Hex(new FileInputStream(fileInfo.path.toFile()));
                    } catch (IOException e) {
                        log.error("error" , e);
                        fileInfo.sha2 = "ERROR";
                    }
                });
                Map<String, List<FileInfo>> mapWithSameSha2 = fileInfos.stream().collect(Collectors.groupingBy(FileInfo::getSha2));
                mapWithSameSha2.forEach((s, fileInfos1) -> {
                    if (fileInfos.size() >= 2) {
                        writeData("DUPLICATE " + fileInfos1);
                    }
                });

            }
        });
        closeWriter();
        return result;
    }

    public void writeData(String data) {
        try {
            writer.write(data);
            writer.newLine();
            writer.flush();  // Ensures data is written to the file immediately
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeWriter() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
