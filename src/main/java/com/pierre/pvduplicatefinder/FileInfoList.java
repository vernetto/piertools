package com.pierre.pvduplicatefinder;

import java.util.ArrayList;
import java.util.List;

public class FileInfoList extends ArrayList<FileInfo> {
    public FileInfoList(List<FileInfo> fileInfos) {
        super(fileInfos);
    }
    public FileInfoList() {

    }

    public boolean containsPath(String thePath) {
        return this.stream()
                .anyMatch(fileInfo -> fileInfo.getPath().toString().contains(thePath));
    }

    public boolean containsBothPath(String thePath1, String thePath2) {
        return this.stream()
                .anyMatch(fileInfo -> {
                    String path = fileInfo.getPath().toString();
                    return path.contains(thePath1) || path.contains(thePath2);
                });
    }

}
