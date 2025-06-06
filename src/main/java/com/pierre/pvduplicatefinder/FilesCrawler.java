package com.pierre.pvduplicatefinder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FilesCrawler {
    int count = 0;

    List<FileInfo> findAllFileInfo(String rootFolder) throws IOException {
        return findAllFileInfo(Paths.get(rootFolder));
    }

    List<FileInfo> findAllFileInfo(Path rootFolder) throws IOException {
        List<FileInfo> fileInfos = new ArrayList<>();
        //log.info("reading files in " + rootFolder.getFileName());
        String lowerCase = rootFolder.toString().toLowerCase();
        if (lowerCase.endsWith("\\itunes") || lowerCase.endsWith("\\calibre") || lowerCase.endsWith("\\audio")) {
            return fileInfos;
        }
        if (rootFolder == null || rootFolder.toFile() == null || rootFolder.toFile().listFiles() == null) {
            //log.info("empty folder or path");
            return fileInfos;
        }
        for (File file : rootFolder.toFile().listFiles()) {
            Path thisPath = file.toPath();
            if (file.isDirectory() && !(file.getPath().equals(rootFolder.toFile().getPath()))) {
                fileInfos.addAll(findAllFileInfo(thisPath));
            } else {
                try {
                    FileChannel fileChannel = FileChannel.open(thisPath);
                    FileInfo fileInfo = FileInfo.builder().path(thisPath).size(fileChannel.size()).time(Files.getLastModifiedTime(thisPath)).fileAction(FileAction.NOTHING).build();
                    fileInfos.add(fileInfo);
                } catch (IOException e) {
                    log.error("error", e);
                }

            }
        }
        int countAfter = count + fileInfos.size();
        if ((countAfter / 1000) != (count / 1000)) {
            log.info("count is now " + countAfter + " while scanning " + rootFolder.toString());
        }
        count = countAfter;
        return fileInfos;
    }


}
