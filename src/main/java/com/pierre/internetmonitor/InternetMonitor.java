package com.pierre.internetmonitor;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class InternetMonitor {

    private static final String LOG_FILE = "connection_failures.log";
    private static final String TEST_HOST = "8.8.8.8";  // Google DNS
    private static final int INTERVAL_SECONDS = 30;

    public static void main(String[] args) {
        log.info("Starting InternetMonitor. Checking every " + INTERVAL_SECONDS + " seconds...");
        while (true) {
            boolean isOnline = checkConnection();
            if (!isOnline) {
                logFailure();
            }
            else {
                log.debug("Internet connection is stable.");
            }
            try {
                Thread.sleep(INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
                log.error("Interrupted, exiting.");
                break;
            }
        }
    }

    private static boolean checkConnection() {
        try {
            InetAddress address = InetAddress.getByName(TEST_HOST);
            return address.isReachable(5000);  // timeout: 5 seconds
        } catch (IOException e) {
            return false;
        }
    }

    private static void logFailure() {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = "Connection failed at " + time;
        log.warn(line);

        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(line);
        } catch (IOException e) {
            log.error("Failed to write to log file: " + e.getMessage());
        }
    }
}
