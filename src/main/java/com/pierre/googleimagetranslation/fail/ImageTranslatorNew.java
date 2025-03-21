package com.pierre.googleimagetranslation.fail;


import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v120.network.Network;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ImageTranslatorNew - A program to automate sending an image to Google Translate
 * and downloading the translated result using Selenium with CDP support.
 */
public class ImageTranslatorNew {

    private static final String GOOGLE_TRANSLATE_IMAGE_URL = "https://translate.google.com/?sl=en&tl=ru&op=images";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    public static void main(String[] args) {

        String imagePath = "D:\\pierre\\ingleseGiocando\\Primo volume\\INGLESE_001.jpg";
        File imageFile = new File(imagePath);

        if (!imageFile.exists() || !imageFile.isFile()) {
            System.out.println("Error: Image file does not exist or is not a file: " + imagePath);
            System.exit(1);
        }

        // Check if the file is a supported image type
        String fileName = imageFile.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            System.out.println("Error: Unsupported file type. Only JPG, JPEG, and PNG files are supported.");
            System.exit(1);
        }

        try {
            new ImageTranslatorNew().translateImage(imageFile);
        } catch (Exception e) {
            System.err.println("Error during translation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Translates the given image using Google Translate.
     *
     * @param imageFile The image file to translate
     * @throws IOException If an I/O error occurs
     */
    public void translateImage(File imageFile) throws IOException {
        System.out.println("Connecting to Chrome with remote debugging on port 9222...");

        // Set up Chrome options to connect to existing Chrome instance
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        options.setPageLoadTimeout(Duration.ofSeconds(60));

        // Initialize the Chrome driver
        ChromeDriver driver = null;

        try {
            driver = new ChromeDriver(options);

            // Set up DevTools for CDP
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            // Enable network monitoring
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

            // Set up a wait timeout
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));

            // Navigate to Google Translate image translation page
            System.out.println("Navigating to Google Translate...");
            driver.get(GOOGLE_TRANSLATE_IMAGE_URL);

            // Make sure we're on the Images tab
            ensureImagesTabSelected(driver, wait);

            // Upload the image
            System.out.println("Uploading image: " + imageFile.getAbsolutePath());
            uploadImage(driver, wait, imageFile);

            // Wait for translation to complete
            System.out.println("Waiting for translation to complete...");
            waitForTranslation(driver, wait);

            // Download the translated image
            System.out.println("Downloading translated image...");
            String outputPath = downloadTranslatedImage(driver, imageFile.getName());

            System.out.println("Translation completed successfully!");
            System.out.println("Translated image saved to: " + outputPath);

        } finally {
            // We don't close the browser since it was started externally
            System.out.println("Disconnected from Chrome");
        }
    }

    /**
     * Ensures that the Images tab is selected in Google Translate.
     *
     * @param driver The WebDriver instance
     * @param wait The WebDriverWait instance
     */
    private void ensureImagesTabSelected(ChromeDriver driver, WebDriverWait wait) {
        // Check if we need to click the Images tab
        if (driver.getCurrentUrl().contains("op=images")) {
            System.out.println("Already on Images tab");
            return;
        }

        System.out.println("Selecting Images tab");
        try {
            WebElement imagesTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Images')]")));
            imagesTab.click();

            // Wait a bit for the UI to update
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            System.out.println("Could not click Images tab: " + e.getMessage());
            // Try to navigate directly to the images URL
            driver.get(GOOGLE_TRANSLATE_IMAGE_URL);
        }
    }

    /**
     * Uploads an image to Google Translate.
     *
     * @param driver The WebDriver instance
     * @param wait The WebDriverWait instance
     * @param imageFile The image file to upload
     * @throws IOException If an I/O error occurs
     */
    private void uploadImage(ChromeDriver driver, WebDriverWait wait, File imageFile) throws IOException {
        // First try clicking the "Browse your files" button
        try {
            WebElement browseButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Browse your files')]")));
            browseButton.click();

            // Wait for the file input to be ready
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Find the file input element
            WebElement fileInput = driver.findElement(By.xpath("//input[@type='file']"));

            // Upload the file
            fileInput.sendKeys(imageFile.getAbsolutePath());

            System.out.println("File upload initiated");

        } catch (Exception e) {
            System.out.println("Could not use button method, trying alternative upload method: " + e.getMessage());

            // Alternative method: Find file input element and upload directly
            try {
                WebElement fileInput = driver.findElement(By.xpath("//input[@type='file']"));
                fileInput.sendKeys(imageFile.getAbsolutePath());
            } catch (Exception e2) {
                System.out.println("Alternative upload method failed: " + e2.getMessage());

                // Try JavaScript approach
                try {
                    System.out.println("Trying JavaScript approach...");

                    // Execute JavaScript to find and make the file input visible
                    driver.executeScript(
                            "const input = document.querySelector('input[type=\"file\"]');" +
                                    "if(input) {" +
                                    "  input.style.display = 'block';" +
                                    "  input.style.opacity = '1';" +
                                    "  input.style.visibility = 'visible';" +
                                    "}");

                    // Try to find the file input again
                    WebElement fileInput = driver.findElement(By.xpath("//input[@type='file']"));
                    fileInput.sendKeys(imageFile.getAbsolutePath());

                } catch (Exception e3) {
                    throw new IOException("Failed to upload image using multiple methods", e3);
                }
            }
        }

        // Wait for the upload to complete and processing to start
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits for the translation to complete.
     *
     * @param driver The WebDriver instance
     * @param wait The WebDriverWait instance
     */
    private void waitForTranslation(ChromeDriver driver, WebDriverWait wait) {
        // Wait for the translation to appear
        try {
            // Check for download button or other indicators that translation is complete
            wait.until(driver1 -> {
                try {
                    return driver1.findElement(By.xpath("//button[contains(text(), 'Download')]")) != null ||
                            driver1.findElement(By.xpath("//button[@aria-label='Download']")) != null ||
                            driver1.findElement(By.cssSelector(".translation-result")) != null;
                } catch (Exception e) {
                    return false;
                }
            });

            System.out.println("Translation completed");

            // Give a little extra time for the UI to fully update
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            System.out.println("Warning: Timed out waiting for translation to complete: " + e.getMessage());
        }
    }

    /**
     * Downloads the translated image.
     *
     * @param driver The WebDriver instance
     * @param originalFileName The original file name
     * @return The path to the saved translated image
     * @throws IOException If an I/O error occurs
     */
    private String downloadTranslatedImage(ChromeDriver driver, String originalFileName) throws IOException {
        // Generate output filename
        String baseName = originalFileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        String outputFileName = baseName + "_translated.png";

        // Try to click the download button if it exists
        boolean downloadButtonClicked = false;
        try {
            WebElement downloadButton = null;
            try {
                downloadButton = driver.findElement(By.xpath("//button[contains(text(), 'Download')]"));
            } catch (Exception e) {
                try {
                    downloadButton = driver.findElement(By.xpath("//button[@aria-label='Download']"));
                } catch (Exception e2) {
                    // No download button found
                }
            }

            if (downloadButton != null) {
                downloadButton.click();
                downloadButtonClicked = true;

                System.out.println("Download button clicked, waiting for download to start...");
                // Wait for the download to start
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            System.out.println("Could not click download button: " + e.getMessage());
        }

        // Try to extract the translated image directly from the page
        try {
            String imageDataUrl = (String) driver.executeScript(
                    "const img = document.querySelector('.translation-result img') || " +
                            "document.querySelector('.translated-image') || " +
                            "document.querySelector('.result-container img'); " +
                            "return img ? img.src : null;"
            );

            if (imageDataUrl != null && imageDataUrl.startsWith("data:image")) {
                System.out.println("Found image data URL, extracting...");

                // Extract base64 data from data URL
                String base64Data = imageDataUrl.substring(imageDataUrl.indexOf(",") + 1);
                byte[] imageData = Base64.getDecoder().decode(base64Data);

                // Save the image
                Path outputPath = Paths.get(outputFileName);
                Files.write(outputPath, imageData);

                System.out.println("Saved translated image from data URL");
                return outputPath.toAbsolutePath().toString();
            }
        } catch (Exception e) {
            System.out.println("Could not extract image data URL: " + e.getMessage());
        }

        // Take screenshot of the translated image as a fallback
        System.out.println("Taking screenshot of translated image as fallback");

        // Try to find the translated image element
        WebElement translatedImage = null;
        try {
            translatedImage = driver.findElement(By.cssSelector(".translation-result img, .translated-image, .result-container img"));
        } catch (Exception e) {
            // No specific image element found, take full screenshot
        }

        byte[] screenshotBytes;
        if (translatedImage != null) {
            // Take screenshot of just the translated image element
            screenshotBytes = translatedImage.getScreenshotAs(OutputType.BYTES);
        } else {
            // Take full page screenshot
            screenshotBytes = driver.getScreenshotAs(OutputType.BYTES);
        }

        // Save the screenshot
        Path outputPath = Paths.get(outputFileName);
        Files.write(outputPath, screenshotBytes);

        return outputPath.toAbsolutePath().toString();
    }
}
