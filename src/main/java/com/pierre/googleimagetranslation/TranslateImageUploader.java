package com.pierre.googleimagetranslation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static com.pierre.googleimagetranslation.GITConstants.IMAGE_TO_TRANSLATE;

public class TranslateImageUploader {

    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // You can persist user profile to avoid seeing popup again:
        options.addArguments("user-data-dir=D:\\temp\\selenium-profile");
        options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            driver.get("https://translate.google.com/?sl=en&tl=ru&op=images");

            // Step 1: Accept cookie/consent popup if present
            try {
                WebElement agreeButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[.//div[contains(text(), 'Accept all') or contains(text(), 'Ich stimme zu')]]")
                ));
                agreeButton.click();
                System.out.println("Clicked on 'Accept all' button.");
            } catch (TimeoutException e) {
                System.out.println("No cookie popup appeared.");
            }

            // Step 2: Locate file input
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));

            // Step 3: Upload image
            File imageFile = new File(IMAGE_TO_TRANSLATE);
            if (!imageFile.exists()) {
                throw new RuntimeException("Image not found: " + imageFile.getAbsolutePath());
            }
            fileInput.sendKeys(imageFile.getAbsolutePath());
            System.out.println("Image uploaded.");

            // Step 4: Wait for translation to load
            Thread.sleep(5000); // Ideally replaced by wait for some result element

            // Step 5: Screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File output = new File("translated-result.png");
            Files.copy(screenshot.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + output.getAbsolutePath());

        } finally {
            driver.quit();
        }
    }
}
