package com.pierre.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeSubtitleFetcher {

    public static String getSubtitleUrl(String videoId) throws IOException {
        String url = "https://www.youtube.com/watch?v=" + videoId;
        Document doc = Jsoup.connect(url).get();

        // Extract the subtitle URL (typically hidden in JavaScript)
        Pattern pattern = Pattern.compile("\"captionTracks\":\\[\\{\"baseUrl\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(doc.html());

        if (matcher.find()) {
            return matcher.group(1).replace("\\u0026", "&");
        } else {
            throw new IOException("Subtitle URL not found");
        }
    }

    public static void main(String[] args) {
        String videoId = "KaOJyVHEeho";
        try {
            String subtitleUrl = getSubtitleUrl(videoId);
            System.out.println("Subtitle URL: " + subtitleUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
