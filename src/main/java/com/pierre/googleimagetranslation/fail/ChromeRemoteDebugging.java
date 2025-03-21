package com.pierre.googleimagetranslation.fail;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChromeRemoteDebugging {

    private static final String DEBUGGING_URL = "http://localhost:9222/json";
    private WebSocketClient webSocketClient;

    public static void main(String[] args) throws Exception {
        ChromeRemoteDebugging chromeDebugger = new ChromeRemoteDebugging();
        String webSocketDebuggerUrl = chromeDebugger.getWebSocketDebuggerUrl();
        if (webSocketDebuggerUrl != null) {
            chromeDebugger.connectToChrome(webSocketDebuggerUrl);
            Thread.sleep(2000); // Wait for connection
            chromeDebugger.navigateToUrl("https://www.google.com");
        } else {
            System.err.println("No available WebSocket debugging URL.");
        }
    }

    private String getWebSocketDebuggerUrl() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(DEBUGGING_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("webSocketDebuggerUrl")) {
                return jsonObject.get("webSocketDebuggerUrl").getAsString();
            }
        }
        return null;
    }

    private void connectToChrome(String webSocketUrl) {
        try {
            webSocketClient = new WebSocketClient(new URI(webSocketUrl)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to Chrome Debugging Protocol.");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Received message: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToUrl(String url) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            JsonObject command = new JsonObject();
            command.addProperty("id", 1);
            command.addProperty("method", "Page.navigate");

            JsonObject params = new JsonObject();
            params.addProperty("url", url);
            command.add("params", params);

            webSocketClient.send(command.toString());
            System.out.println("Navigating to " + url);
        } else {
            System.err.println("WebSocket connection is not open.");
        }
    }
}
