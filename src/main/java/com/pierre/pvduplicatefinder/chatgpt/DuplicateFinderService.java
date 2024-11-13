package com.pierre.pvduplicatefinder.chatgpt;

        import org.springframework.stereotype.Service;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.IOException;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.HashMap;
        import java.util.Map;

        @Service
        public class DuplicateFinderService {

            public void findDuplicates(String directoryPath) {
                Map<String, String> fileHashes = new HashMap<>();
                File directory = new File(directoryPath);

                if (!directory.isDirectory()) {
                    System.out.println("Invalid directory path provided");
                    return;
                }

                for (File file : directory.listFiles()) {
                    if (file.isFile()) {
                        try {
                            String hash = calculateHash(file);
                            if (fileHashes.containsKey(hash)) {
                                System.out.println("Duplicate found: " + file.getAbsolutePath() +
                                        " and " + fileHashes.get(hash));
                            } else {
                                fileHashes.put(hash, file.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            System.out.println("Error processing file " + file.getAbsolutePath() + ": " + e.getMessage());
                        }
                    }
                }
            }

            private String calculateHash(File file) throws IOException, NoSuchAlgorithmException {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] byteArray = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(byteArray)) != -1) {
                        digest.update(byteArray, 0, bytesRead);
                    }
                }
                byte[] bytes = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            }
        }