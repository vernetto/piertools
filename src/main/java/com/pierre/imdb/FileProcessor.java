package com.pierre.imdb;

import java.io.File;
import java.util.Map;

public class FileProcessor {
    private MovieService movieService;
    private FileMatcher fileMatcher;

    public FileProcessor(MovieService movieService, FileMatcher fileMatcher) {
        this.movieService = movieService;
        this.fileMatcher = fileMatcher;
    }

    public void processFiles(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                if (fileMatcher.isVideoFile(filename)) {
                    for (Map.Entry<String, String> entry : movieService.getMovies().entrySet()) {
                        String title = entry.getKey();
                        String year = entry.getValue();

                        if (fileMatcher.hasMatchingTitle(filename, title) &&
                                fileMatcher.hasMatchingYear(filename, year)) {
                            System.out.println("Match found: " + filename + " -> " + title + " (" + year + ")");
                        }
                    }
                }
            }
        }
    }
}
