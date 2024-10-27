package com.pierre.imdb;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MovieService {
    private Map<String, String> movies = new HashMap<>();

    public void loadMovies(String csvFilePath) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] headers = reader.readNext(); // Read the header row

            int titleIndex = -1;
            int yearIndex = -1;

            // Find indexes for "Title" and "Year" columns
            for (int i = 0; i < headers.length; i++) {
                if ("Title".equalsIgnoreCase(headers[i])) {
                    titleIndex = i;
                } else if ("Year".equalsIgnoreCase(headers[i])) {
                    yearIndex = i;
                }
            }

            // Check if required columns are present
            if (titleIndex == -1 || yearIndex == -1) {
                throw new IllegalArgumentException("CSV file must contain 'Title' and 'Year' columns.");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                String title = line[titleIndex];
                String year = line[yearIndex];

                if (title != null && year != null) {
                    movies.put(title.toLowerCase(), year); // Use lowercase to facilitate case-insensitive matching
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getMovies() {
        return movies;
    }
}

