package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

@Data
@Builder
@JsonDeserialize(builder = FileInfo.FileInfoBuilder.class)
public class FileInfo {
    Path path;
    @JsonDeserialize(using = FileTimeDeserializer.class)
    FileTime time;

    long size;
    String extension;
    String sha2;
    FileAction fileAction = FileAction.NOTHING;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public String getTime() {
        return time != null ? time.toString() : null;
    }

    // Define the builder class to help Jackson with deserialization
    @JsonPOJOBuilder(withPrefix = "")
    public static class FileInfoBuilder {
    }
}
