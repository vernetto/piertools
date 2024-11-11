package com.pierre.pvduplicatefinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DuplicateCollection {
    List<File> duplicateFiles = new ArrayList<>();

    public void add(File file) {
        duplicateFiles.add(file);
    }
}
