package com.pierre.pvduplicatefinder;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

@Data
@Builder
public class FileInfo {
    Path path;
    FileTime time;
    long size;
    String extension;
    String sha2;
}
