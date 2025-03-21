package com.pierre.googleimagetranslation.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class GoogleTranslateImageUpload {

    public static void main(String[] args) throws Exception {
        // Setup ChromeDriver
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");
        WebDriver driver = new ChromeDriver(options);

        try {
            // Go to Google Translate Image page
            driver.get("https://translate.google.com/?sl=en&tl=ru&op=images");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Wait and find the upload button
            WebElement uploadButton = driver.findElement(By.xpath("//input[@type='file']"));

            // Specify the file to upload
            File imageFile = new File("D:\\pierre\\ingleseGiocando\\Primo volume\\INGLESE_001.jpg"); // replace with your path
            if (!imageFile.exists()) {
                throw new RuntimeException("Image not found: " + imageFile.getAbsolutePath());
            }

            // Upload the image
            uploadButton.sendKeys(imageFile.getAbsolutePath());

            // Wait for translation to appear (may need to adjust logic here)
            Thread.sleep(5000); // better replaced with WebDriverWait in real case

            // Take screenshot of result
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File output = new File("translated-result.png");
            Files.copy(screenshot.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Translated result saved to: " + output.getAbsolutePath());

        } finally {
            // Close browser
            driver.quit();
        }
    }
}
