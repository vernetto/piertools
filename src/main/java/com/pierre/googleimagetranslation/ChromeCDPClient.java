package com.pierre.googleimagetranslation;

import com.google.gson.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

public class ChromeCDPClient {

    private static final String CHROME_JSON_URL = "http://localhost:9222/json";
    private static int messageId = 1;

    public static void main(String[] args) throws Exception {
        String wsUrl = getWebSocketDebuggerUrl();
        if (wsUrl == null) {
            System.err.println("No WebSocket URL found.");
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);

        WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to Chrome.");

                // Enable Page domain
                sendCdp("Page.enable", null);

                // Enable Screencast domain (optional)
                sendCdp("DOM.enable", null);
                sendCdp("Runtime.enable", null);

                // Navigate to Translate Images
                JsonObject params = new JsonObject();
                params.addProperty("url", "https://translate.google.com/?sl=en&tl=ru&op=images");
                sendCdp("Page.navigate", params);

                System.out.println("Navigation requested.");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("<< " + message);

                JsonObject msg = JsonParser.parseString(message).getAsJsonObject();

                if (msg.has("method") && msg.get("method").getAsString().equals("Page.loadEventFired")) {
                    System.out.println("Page loaded, taking screenshot...");

                    JsonObject params = new JsonObject();
                    params.addProperty("format", "png");
                    sendCdp("Page.captureScreenshot", params);
                }

                if (msg.has("id") && msg.get("id").getAsInt() == messageId - 1) {
                    JsonObject result = msg.getAsJsonObject("result");
                    if (result != null && result.has("data")) {
                        String base64Screenshot = result.get("data").getAsString();
                        saveBase64ToFile(base64Screenshot, "translated-screenshot.png");
                        System.out.println("Screenshot saved!");
                        latch.countDown(); // Quit
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            private void sendCdp(String method, JsonObject params) {
                JsonObject message = new JsonObject();
                message.addProperty("id", messageId++);
                message.addProperty("method", method);
                if (params != null) {
                    message.add("params", params);
                }
                send(message.toString());
            }
        };

        client.connect();
        latch.await(); // Wait for screenshot
        client.close();
    }

    private static String getWebSocketDebuggerUrl() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(CHROME_JSON_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonArray tabs = JsonParser.parseString(response.body()).getAsJsonArray();

        for (JsonElement el : tabs) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("webSocketDebuggerUrl")) {
                return obj.get("webSocketDebuggerUrl").getAsString();
            }
        }
        return null;
    }

    private static void saveBase64ToFile(String base64, String fileName) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            try (FileOutputStream out = new FileOutputStream(new File(fileName))) {
                out.write(decoded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
