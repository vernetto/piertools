# Image Translator

This Java application automates the process of translating text in images using Google Translate. It connects to a Chrome browser with remote debugging enabled, uploads an image to Google Translate's image translation feature, and downloads the translated image.

## Prerequisites

- Java 11 or higher
- Maven
- Google Chrome browser
- Chrome must be started with remote debugging enabled on port 9222

## How to Start Chrome with Remote Debugging

Start Chrome with the following command:

```bash
google-chrome --remote-debugging-port=9222
```

Or on Windows:

```bash
"C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222
```

## Building the Application

```bash
cd image_translator
mvn clean package
```

This will create a JAR file with dependencies in the `target` directory.

## Running the Application

```bash
java -jar target/image-translator-1.0-SNAPSHOT-jar-with-dependencies.jar <source-image-path> <output-image-path>
```

Example:
```bash
java -jar target/image-translator-1.0-SNAPSHOT-jar-with-dependencies.jar test_images/sample.jpg translated_image.jpg
```

## Supported Image Formats

- JPEG (.jpg, .jpeg)
- PNG (.png)
- WebP (.webp)

## How It Works

1. Connects to Chrome browser with remote debugging enabled on port 9222
2. Navigates to Google Translate's image translation page
3. Uploads the specified image
4. Waits for translation to complete
5. Downloads the translated image and saves it to the specified output path

## Implementation Details

The application uses the Chrome DevTools Protocol Java Client library (kklisura/chrome-devtools-java-client) to interact with Chrome. The main components are:

- **ImageTranslator**: The main class that orchestrates the translation process
- **connectToChrome()**: Connects to Chrome with remote debugging enabled
- **navigateToTranslatePage()**: Navigates to Google Translate's image translation page
- **uploadImage()**: Uploads the source image using JavaScript injection
- **downloadTranslatedImage()**: Captures and saves the translated image

## Troubleshooting

- Make sure Chrome is running with remote debugging enabled on port 9222
- If the application fails to upload the image automatically, it will prompt you to manually select the file in the file dialog
- Ensure the source image exists and is in a supported format
- Check that you have write permissions for the output image path

## Notes

- The application is designed to work with English to Russian translation by default
- To change the source or target language, modify the GOOGLE_TRANSLATE_URL constant in the code
