package com.pierre.googleimagetranslation.fail;

import com.github.kklisura.cdt.launch.ChromeLauncher;
import com.github.kklisura.cdt.protocol.commands.Page;
import com.github.kklisura.cdt.protocol.commands.Runtime;
import com.github.kklisura.cdt.protocol.types.runtime.Evaluate;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.ChromeService;
import com.github.kklisura.cdt.services.types.ChromeTab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ImageTranslator - A tool to automate image translation using Google Translate
 * Connects to a Chrome browser with remote debugging enabled on port 9222
 */
public class ImageTranslator {

    private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/?sl=en&tl=ru&op=images";
    private static final int MAX_WAIT_ATTEMPTS = 15;
    private static final int WAIT_INTERVAL = 2000; // milliseconds
    private static final int WAIT_TIMEOUT = 30; // seconds

    private ChromeDevToolsService devToolsService;
    private String sourceImagePath;
    private String outputImagePath;

    public ImageTranslator(String sourceImagePath, String outputImagePath) {
        this.sourceImagePath = sourceImagePath;
        this.outputImagePath = outputImagePath;
    }

    /**
     * Main method to run the image translator
     */
    public static void main(String[] args) {

        String sourceImagePath = "D:\\pierre\\ingleseGiocando\\Primo volume\\INGLESE_001.jpg";
        String outputImagePath = "D:\\pierre\\ingleseGiocando\\Primo volume\\INGLESE_001_TRANSLATED.jpg";

        // Validate source image exists
        File sourceImage = new File(sourceImagePath);
        if (!sourceImage.exists() || !sourceImage.isFile()) {
            System.err.println("Source image does not exist: " + sourceImagePath);
            System.exit(1);
        }

        // Check if file is a supported image type
        String fileName = sourceImage.getName().toLowerCase();
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".webp"))) {
            System.err.println("Unsupported file type. Supported types: .jpg, .jpeg, .png, .webp");
            System.exit(1);
        }

        try {
            ImageTranslator translator = new ImageTranslator(sourceImagePath, outputImagePath);
            translator.translate();
        } catch (Exception e) {
            System.err.println("Error during translation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Performs the image translation process
     */
    public void translate() throws Exception {
        System.out.println("Starting image translation process...");
        System.out.println("Connecting to Chrome with remote debugging on port 9222...");

        try {
            // Connect to existing Chrome instance with remote debugging enabled
            connectToChrome();

            // Navigate to Google Translate image translation page
            navigateToTranslatePage();

            // Upload the image
            uploadImage();

            // Wait for translation to complete and download the result
            downloadTranslatedImage();

            System.out.println("Translation completed successfully!");
            System.out.println("Translated image saved to: " + outputImagePath);

        } finally {
            // Clean up resources
            if (devToolsService != null && !devToolsService.isClosed()) {
                devToolsService.close();
            }
        }
    }

    /**
     * Connects to Chrome browser with remote debugging enabled
     */
    private void connectToChrome() throws Exception {
        // Create chrome launcher
        ChromeLauncher launcher = new ChromeLauncher();

        // Connect to existing Chrome instance with remote debugging
        ChromeService chromeService = launcher.launch(false);

        // Get the first tab or create a new one
        ChromeTab tab = chromeService.getTabs().isEmpty()
                ? chromeService.createTab()
                : chromeService.getTabs().get(0);

        // Get DevTools service
        devToolsService = chromeService.createDevToolsService(tab);
    }

    /**
     * Navigates to Google Translate image translation page
     */
    private void navigateToTranslatePage() throws InterruptedException {
        System.out.println("Navigating to Google Translate...");

        Page page = devToolsService.getPage();
        page.enable();

        // Set up event handler for page load
        CountDownLatch pageLoadLatch = new CountDownLatch(1);
        page.onLoadEventFired(event -> pageLoadLatch.countDown());

        // Navigate to Google Translate
        page.navigate(GOOGLE_TRANSLATE_URL);

        // Wait for page to load
        if (!pageLoadLatch.await(WAIT_TIMEOUT, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for page to load");
        }

        // Make sure we're on the image translation tab
        ensureImageTabSelected();

        System.out.println("Google Translate page loaded");
    }

    /**
     * Ensures the "Images" tab is selected on Google Translate
     */
    private void ensureImageTabSelected() throws InterruptedException {
        Runtime runtime = devToolsService.getRuntime();
        runtime.enable();

        // Check if we need to click the Images tab
        String checkImagesTabScript =
                "document.querySelector('button[data-tab=\"images\"]') && " +
                        "!document.querySelector('button[data-tab=\"images\"].VfPpkd-LgbsSe-OWXEXe-YbohUe')";

        Evaluate evaluation = runtime.evaluate(checkImagesTabScript);

        if (evaluation.getResult().getValue() != null &&
                Boolean.TRUE.equals(evaluation.getResult().getValue())) {

            System.out.println("Clicking on Images tab...");

            // Click the Images tab
            String clickImagesTabScript =
                    "document.querySelector('button[data-tab=\"images\"]').click()";
            runtime.evaluate(clickImagesTabScript);

            // Wait for tab to be selected
            Thread.sleep(1000);
        }
    }

    /**
     * Uploads the image to Google Translate
     */
    private void uploadImage() throws IOException, InterruptedException {
        System.out.println("Uploading image: " + sourceImagePath);

        // Read the image file as base64
        byte[] imageBytes = Files.readAllBytes(Paths.get(sourceImagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Runtime runtime = devToolsService.getRuntime();

        // Determine the MIME type based on file extension
        String fileName = new File(sourceImagePath).getName().toLowerCase();
        String mimeType = "image/jpeg"; // default
        if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".webp")) {
            mimeType = "image/webp";
        }

        // Try multiple approaches to upload the file
        boolean uploadSuccess = false;

        // Approach 1: Find and use the file input directly
        String findFileInputScript =
                "const fileInput = document.querySelector('input[type=\"file\"]');" +
                        "return fileInput ? true : false;";

        Evaluate hasFileInput = runtime.evaluate(findFileInputScript);

        if (hasFileInput.getResult().getValue() != null &&
                Boolean.TRUE.equals(hasFileInput.getResult().getValue())) {

            System.out.println("Found file input element, attempting direct upload...");

            // Find the file input element and set its value
            String injectFileScript =
                    "const fileInput = document.querySelector('input[type=\"file\"]');" +
                            "try {" +
                            "  const dataTransfer = new DataTransfer();" +
                            "  const file = new File([Uint8Array.from(atob('" + base64Image + "'), c => c.charCodeAt(0))], '" +
                            new File(sourceImagePath).getName() + "', {type: '" + mimeType + "'});" +
                            "  dataTransfer.items.add(file);" +
                            "  fileInput.files = dataTransfer.files;" +
                            "  const event = new Event('change', {bubbles: true});" +
                            "  fileInput.dispatchEvent(event);" +
                            "  return true;" +
                            "} catch (e) {" +
                            "  console.error('Error during file upload:', e);" +
                            "  return false;" +
                            "}";

            Evaluate evaluation = runtime.evaluate(injectFileScript);

            if (evaluation.getResult().getValue() != null &&
                    Boolean.TRUE.equals(evaluation.getResult().getValue())) {
                uploadSuccess = true;
                System.out.println("Direct file upload successful");
            }
        }

        // Approach 2: Click the browse button and try to inject file
        if (!uploadSuccess) {
            System.out.println("Trying alternative upload method...");

            // Click the browse button
            String clickBrowseButtonScript =
                    "const browseButton = document.querySelector('button[aria-label=\"Browse your files\"]');" +
                            "if (browseButton) {" +
                            "  browseButton.click();" +
                            "  return true;" +
                            "} else {" +
                            "  return false;" +
                            "}";

            Evaluate clickResult = runtime.evaluate(clickBrowseButtonScript);

            if (clickResult.getResult().getValue() != null &&
                    Boolean.TRUE.equals(clickResult.getResult().getValue())) {

                // Wait for the file dialog to appear
                Thread.sleep(1000);

                // Try to find the file input that might have appeared
                String findNewFileInputScript =
                        "const fileInput = document.querySelector('input[type=\"file\"]');" +
                                "if (fileInput) {" +
                                "  try {" +
                                "    const dataTransfer = new DataTransfer();" +
                                "    const file = new File([Uint8Array.from(atob('" + base64Image + "'), c => c.charCodeAt(0))], '" +
                                new File(sourceImagePath).getName() + "', {type: '" + mimeType + "'});" +
                                "    dataTransfer.items.add(file);" +
                                "    fileInput.files = dataTransfer.files;" +
                                "    const event = new Event('change', {bubbles: true});" +
                                "    fileInput.dispatchEvent(event);" +
                                "    return true;" +
                                "  } catch (e) {" +
                                "    console.error('Error during file upload:', e);" +
                                "    return false;" +
                                "  }" +
                                "} else {" +
                                "  return false;" +
                                "}";

                Evaluate newEvaluation = runtime.evaluate(findNewFileInputScript);

                if (newEvaluation.getResult().getValue() != null &&
                        Boolean.TRUE.equals(newEvaluation.getResult().getValue())) {
                    uploadSuccess = true;
                    System.out.println("Alternative file upload successful");
                }
            }
        }

        // Approach 3: If all automated methods fail, ask for manual intervention
        if (!uploadSuccess) {
            System.out.println("Automated file upload methods failed.");
            System.out.println("Please manually select the file in the file dialog that appears...");

            // Click the browse button one more time
            String finalClickScript =
                    "const browseButton = document.querySelector('button[aria-label=\"Browse your files\"]');" +
                            "if (browseButton) browseButton.click();";
            runtime.evaluate(finalClickScript);

            // Wait for manual selection
            System.out.println("Waiting for manual file selection (15 seconds)...");
            Thread.sleep(15000); // Give user time to select the file
        }

        System.out.println("Image upload process completed, waiting for translation...");

        // Wait for translation to process
        Thread.sleep(3000);
    }

    /**
     * Downloads the translated image
     */
    private void downloadTranslatedImage() throws IOException, InterruptedException {
        System.out.println("Waiting for translation to complete...");

        // Wait for the translation to complete
        Thread.sleep(3000);

        Runtime runtime = devToolsService.getRuntime();

        // Check if translation is complete by looking for the translated image
        String checkTranslationScript =
                "const translatedImg = document.querySelector('.image-viewer-translated img');" +
                        "return translatedImg ? true : false;";

        boolean translationComplete = false;
        int attempts = 0;

        while (!translationComplete && attempts < MAX_WAIT_ATTEMPTS) {
            Evaluate evaluation = runtime.evaluate(checkTranslationScript);

            if (evaluation.getResult().getValue() != null &&
                    Boolean.TRUE.equals(evaluation.getResult().getValue())) {
                translationComplete = true;
            } else {
                System.out.println("Translation in progress, waiting... (Attempt " + (attempts + 1) +
                        " of " + MAX_WAIT_ATTEMPTS + ")");
                Thread.sleep(WAIT_INTERVAL);
                attempts++;
            }
        }

        if (!translationComplete) {
            throw new RuntimeException("Timeout waiting for translation to complete");
        }

        System.out.println("Translation completed, downloading image...");

        // Get the translated image as base64
        String getTranslatedImageScript =
                "const img = document.querySelector('.image-viewer-translated img');" +
                        "if (img) {" +
                        "  const canvas = document.createElement('canvas');" +
                        "  canvas.width = img.naturalWidth;" +
                        "  canvas.height = img.naturalHeight;" +
                        "  const ctx = canvas.getContext('2d');" +
                        "  ctx.drawImage(img, 0, 0);" +
                        "  return canvas.toDataURL('image/png').split(',')[1];" +
                        "} else {" +
                        "  return null;" +
                        "}";

        Evaluate evaluation = runtime.evaluate(getTranslatedImageScript);

        if (evaluation.getResult().getValue() == null) {
            throw new RuntimeException("Failed to get translated image");
        }

        // Save the base64 image to file
        String base64Image = (String) evaluation.getResult().getValue();
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        try (FileOutputStream fos = new FileOutputStream(outputImagePath)) {
            fos.write(imageBytes);
        }
    }
}
