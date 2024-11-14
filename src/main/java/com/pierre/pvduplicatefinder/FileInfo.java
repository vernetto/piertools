package com.pierre.pvduplicatefinder;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
public class FileInfo {
    Path path;
    FileTime time;
    long size;
    String extension;
    String sha2;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public String getTime() {
        return time != null ? time.toString() : null;
    }
}
