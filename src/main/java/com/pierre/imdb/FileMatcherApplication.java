package com.pierre.imdb;

import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;

public class FileMatcherApplication {

    private MovieService movieService = new MovieService();
    private FileMatcher fileMatcher = new FileMatcher();

    private FileProcessor fileProcessor = new FileProcessor(movieService, fileMatcher);

    public static void main(String[] args) throws IOException, CsvValidationException {
        FileMatcherApplication fileMatcherApplication = new FileMatcherApplication();
        fileMatcherApplication.run();
    }

    private void run() throws IOException, CsvValidationException {
        String csvFilePath = "D:\\pierre\\downloads\\imdbrating20241026.csv";
        String directoryPath = "X:\\pierre\\emule\\Incoming";

        movieService.loadMovies(csvFilePath);
        fileProcessor.processFiles(directoryPath);
    }
}
