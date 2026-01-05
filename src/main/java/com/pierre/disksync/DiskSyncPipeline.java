package com.pierre.disksync;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DiskSyncPipeline {

    private static final Path SOURCE = Path.of("F:\\pierre\\");
    private static final Path TARGET = Path.of("H:\\pierre\\");
    private static final Path TEMP   = Path.of("D:\\temp");

    private static final long MAX_TEMP_BYTES = 100L * 1024 * 1024 * 1024; // 100 GB

    private static final BlockingQueue<Path> queue = new LinkedBlockingQueue<>();
    private static final AtomicLong tempBytesUsed = new AtomicLong(0);

    public static void main(String[] args) throws Exception {

        Files.createDirectories(TEMP);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(new Producer());
        executor.submit(new Consumer());

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    // =========================
    // Producer
    // =========================
    static class Producer implements Runnable {

        @Override
        public void run() {
            try {
                Files.walkFileTree(SOURCE, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {

                        if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;

                        Path relative = SOURCE.relativize(file);
                        Path targetFile = TARGET.resolve(relative);

                        if (Files.exists(targetFile)) {
                            return FileVisitResult.CONTINUE;
                        }

                        long size = attrs.size();

                        // wait until enough space
                        try {
                            waitForSpace(size);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        Path tempFile = TEMP.resolve(relative);
                        Files.createDirectories(tempFile.getParent());

                        Files.copy(file, tempFile, StandardCopyOption.COPY_ATTRIBUTES);

                        tempBytesUsed.addAndGet(size);
                        try {
                            queue.put(tempFile);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("[PRODUCER] Copied to temp: " + tempFile);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void waitForSpace(long size) throws InterruptedException {
            while (tempBytesUsed.get() + size > MAX_TEMP_BYTES) {
                Thread.sleep(1000);
            }
        }
    }

    // =========================
    // Consumer
    // =========================
    static class Consumer implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Path tempFile = queue.take();

                    Path relative = TEMP.relativize(tempFile);
                    Path targetFile = TARGET.resolve(relative);

                    Files.createDirectories(targetFile.getParent());
                    Files.copy(tempFile, targetFile, StandardCopyOption.COPY_ATTRIBUTES);

                    long size = Files.size(tempFile);
                    Files.delete(tempFile);
                    cleanupEmptyDirs(tempFile.getParent());

                    tempBytesUsed.addAndGet(-size);

                    System.out.println("[CONSUMER] Copied to target and deleted temp: " + targetFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void cleanupEmptyDirs(Path dir) throws IOException {
            while (!dir.equals(TEMP)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                    if (ds.iterator().hasNext()) return;
                }
                Files.delete(dir);
                dir = dir.getParent();
            }
        }
    }
}
