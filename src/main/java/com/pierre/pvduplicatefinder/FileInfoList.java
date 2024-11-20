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

    public boolean containsBothPaths(String thePath1, String thePath2) {
        boolean foundPath1 = false;
        boolean foundPath2 = false;

        for (FileInfo fileInfo : this) {
            String path = fileInfo.getPath().toString();

            if (path.contains(thePath1)) {
                foundPath1 = true;
            }
            if (path.contains(thePath2)) {
                foundPath2 = true;
            }

            // Early exit if both conditions are met
            if (foundPath1 && foundPath2) {
                return true;
            }
        }

        return false;
    }

}
