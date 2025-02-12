package com.pierre.youtubesuttitletranslator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
public class SubtitleTranslator {

    public static void main(String[] args) {
        String inputFilePath = "subtitles_en.srt";  // English SRT file
        String outputFilePath = "subtitles_en_ru.srt";  // Bilingual output

        try {
            List<String> lines = Files.readAllLines(Paths.get(inputFilePath), StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8));

            StringBuilder subtitleBlock = new StringBuilder();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    processSubtitleBlock(subtitleBlock.toString(), writer);
                    subtitleBlock.setLength(0);
                } else {
                    subtitleBlock.append(line).append("\n");
                }
            }
            if (subtitleBlock.length() > 0) {
                processSubtitleBlock(subtitleBlock.toString(), writer);
            }

            writer.close();
            System.out.println("Translation completed! Output saved as " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processSubtitleBlock(String subtitleBlock, BufferedWriter writer) throws IOException {
        log.info(subtitleBlock);
        String[] lines = subtitleBlock.split("\n");
        if (lines.length < 2) return;  // Skip if not a valid subtitle block

        writer.write(lines[0]); // Subtitle index
        writer.newLine();
        writer.write(lines[1]); // Timestamp
        writer.newLine();

        for (int i = 2; i < lines.length; i++) {
            String translatedText = translateText(lines[i], "en", "ru");
            writer.write(lines[i]); // Original text (English)
            writer.newLine();
            writer.write(translatedText); // Corrected Translated text (Russian)
            writer.newLine();
        }
        writer.newLine();
    }

    private static String translateText(String text, String sourceLang, String targetLang) {
        try {
            String url = "https://translate.google.com/m?sl=" + sourceLang + "&tl=" + targetLang + "&q=" + text.replace(" ", "%20");
            Document doc = Jsoup.connect(url).get();

            // Corrected translation extraction
            Element translationElement = doc.select("div[class=translation]").first();
            return (translationElement != null) ? translationElement.text() : text; // Corrected extraction method
        } catch (Exception e) {
            return text; // If translation fails, return original text
        }
    }
}
