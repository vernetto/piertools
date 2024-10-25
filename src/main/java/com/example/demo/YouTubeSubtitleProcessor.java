package com.example.demo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;


public class YouTubeSubtitleProcessor {

    // Step 1: Fetch Subtitle Content
    public static String getSubtitleContent(String subtitleUrl) throws IOException {
        Document doc = Jsoup.connect(subtitleUrl).get();
        Elements texts = doc.select("text");

        StringBuilder content = new StringBuilder();
        for (Element text : texts) {
            content.append(text.text()).append(" ");
        }
        return content.toString();
    }

    // Step 2: Divide Text into Paragraphs
    public static List<String> divideIntoParagraphs(String encodedContent) {
        String content = StringEscapeUtils.unescapeHtml4(encodedContent);

        List<String> paragraphs = new ArrayList<>();
        int index = 0;

        while (index < content.length()) {
            int endIndex = Math.min(index + 300, content.length());

            // Ensure paragraph ends with a punctuation or appropriate place
            int lastPunctuationIndex = -1;
            for (int i = index; i < endIndex; i++) {
                if (isEndOfSentence(content.charAt(i))) {
                    lastPunctuationIndex = i;
                }
            }

            if (lastPunctuationIndex != -1) {
                endIndex = lastPunctuationIndex + 1; // Set endIndex to just after the punctuation
            } else {
                // No punctuation found, find the nearest uppercase starting word within 300 chars
                for (int i = endIndex; i > index; i--) {
                    if (i < content.length() && Character.isUpperCase(content.charAt(i))) {
                        endIndex = i;
                        break;
                    }
                }
            }

            if (endIndex <= index) {  // Avoid StringIndexOutOfBoundsException
                endIndex = Math.min(index + 300, content.length());
            }

            String paragraph = content.substring(index, endIndex).trim();
            paragraphs.add(paragraph);
            index = endIndex;
        }
        return paragraphs;
    }

    // Helper to check if a character is a sentence terminator
    private static boolean isEndOfSentence(char ch) {
        return ch == '.' || ch == ';' || ch == ':' || ch == '?' || ch == '!';
    }

    // Step 3: Group Paragraphs into Segments
    public static List<List<String>> groupIntoSegments(List<String> paragraphs) {
        List<List<String>> segments = new ArrayList<>();
        List<String> currentSegment = new ArrayList<>();

        for (String paragraph : paragraphs) {
            if (currentSegment.size() >= 15) {
                segments.add(new ArrayList<>(currentSegment));
                currentSegment.clear();
            }
            currentSegment.add(paragraph);
        }

        if (!currentSegment.isEmpty()) {
            segments.add(currentSegment);
        }
        return segments;
    }

    // Step 4: Save to File
    public static void saveToFile(String videoId, List<List<String>> segments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(videoId + ".txt"))) {
            int segmentNumber = 1;
            for (List<String> segment : segments) {
                writer.write("Segment " + segmentNumber++ + ":\n");
                for (String paragraph : segment) {
                    writer.write(paragraph + "\n\n");
                }
                writer.write("\n------------------\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String videoId = "DJ6tXTsjX_A";
        try {
            // Fetch subtitle URL (replace this with actual subtitle URL fetching code)
            String subtitleUrl = getSubtitleUrl(videoId);

            // Fetch subtitle content
            String content = getSubtitleContent(subtitleUrl);
            Path filePath = Paths.get(videoId + ".raw.txt");
            Files.write(filePath, content.getBytes());

            // Divide into paragraphs
            List<String> paragraphs = divideIntoParagraphs(content);

            // Group into segments
            List<List<String>> segments = groupIntoSegments(paragraphs);

            // Save to file
            saveToFile(videoId, segments);

            System.out.println("Subtitle processing complete for video ID: " + videoId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example method to retrieve subtitle URL (from previous example)
    private static String getSubtitleUrl(String videoId) throws IOException {
        String url = "https://www.youtube.com/watch?v=" + videoId;
        Document doc = Jsoup.connect(url).get();

        Pattern pattern = Pattern.compile("\"captionTracks\":\\[\\{\"baseUrl\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(doc.html());

        if (matcher.find()) {
            return matcher.group(1).replace("\\u0026", "&");
        } else {
            throw new IOException("Subtitle URL not found");
        }
    }
}
