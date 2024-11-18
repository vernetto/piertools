package com.pierre.pvduplicatefinder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileTimeDeserializer extends JsonDeserializer<FileTime> {
    @Override
    public FileTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return FileTime.from(Instant.parse(value));
    }
}