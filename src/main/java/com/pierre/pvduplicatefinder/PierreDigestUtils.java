package com.pierre.pvduplicatefinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PierreDigestUtils {
    public static String computePartialSHA256(File file, int byteLimit) throws IOException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[byteLimit];
            int bytesRead = fis.read(buffer);

            if (bytesRead > 0) {
                // Only use the bytes that were actually read
                digest.update(Arrays.copyOf(buffer, bytesRead));
            }

            byte[] hash = digest.digest();
            return bytesToHex(hash);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
