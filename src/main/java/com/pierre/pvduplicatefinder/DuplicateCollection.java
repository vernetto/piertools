package com.pierre.pvduplicatefinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DuplicateCollection {
    List<FileInfo> duplicateFiles = new ArrayList<>();

    public void add(FileInfo fileInfo) {
        duplicateFiles.add(fileInfo);
    }
}
