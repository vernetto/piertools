package com.pierre.imdb;

import java.util.regex.*;

public class FileMatcher {
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
    public static final int MATCH_LENGTH = 10;

    public boolean isVideoFile(String filename) {
        return filename.endsWith(".mp4") || filename.endsWith(".mkv") || filename.endsWith(".avi");
    }

    public boolean hasMatchingTitle(String filename, String title) {
        for (int i = 0; i <= filename.length() - MATCH_LENGTH; i++) {
            String substring = filename.substring(i, i + MATCH_LENGTH).toLowerCase();
            if (title.toLowerCase().contains(substring)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMatchingYear(String filename, String year) {
        Matcher matcher = YEAR_PATTERN.matcher(filename);
        while (matcher.find()) {
            if (matcher.group().equals(year)) {
                return true;
            }
        }
        return false;
    }
}
