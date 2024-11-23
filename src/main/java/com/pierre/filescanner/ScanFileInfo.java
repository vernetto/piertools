package com.pierre.filescanner;

public class ScanFileInfo {
    private String fullPath;
    private long size;
    private long lastModified;
    private String sha2Digest;

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getSha2Digest() {
        return sha2Digest;
    }

    public void setSha2Digest(String sha2Digest) {
        this.sha2Digest = sha2Digest;
    }
}
