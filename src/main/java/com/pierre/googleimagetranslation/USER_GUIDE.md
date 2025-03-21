# Image Translator - User Guide

## Overview

This application automates the process of translating text in images using Google Translate. It connects to a Chrome browser with remote debugging enabled, uploads an image to Google Translate's image translation feature, and downloads the translated image.

## Quick Start

1. Start Chrome with remote debugging enabled:
   ```bash
   google-chrome --remote-debugging-port=9222
   ```

2. Run the application using the provided script:
   ```bash
   ./run.sh test_images/sample.jpg translated_image.jpg
   ```

## Prerequisites

- Java 11 or higher
- Maven (will be automatically installed by the run script if missing)
- Google Chrome browser
- Network connectivity to access Google Translate

## Detailed Instructions

### 1. Starting Chrome with Remote Debugging

The application requires Chrome to be running with remote debugging enabled on port 9222. This allows the application to control Chrome programmatically.

**On Linux/macOS:**
```bash
google-chrome --remote-debugging-port=9222
```

**On Windows:**
```bash
"C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222
```

### 2. Running the Application

You can use the provided `run.sh` script which handles all the prerequisites and runs the application:

```bash
./run.sh <source-image-path> <output-image-path>
```

Example:
```bash
./run.sh test_images/sample.jpg translated_image.jpg
```

The script will:
- Check if Chrome is running with remote debugging
- Install Maven if it's not already installed
- Build the application if needed
- Run the application with the provided arguments

### 3. Manual Execution

If you prefer to run the application manually:

1. Build the application:
   ```bash
   cd image_translator
   mvn clean package
   ```

2. Run the application:
   ```bash
   java -jar target/image-translator-1.0-SNAPSHOT-jar-with-dependencies.jar <source-image-path> <output-image-path>
   ```

## Supported Image Formats

The application supports the following image formats:
- JPEG (.jpg, .jpeg)
- PNG (.png)
- WebP (.webp)

## Troubleshooting

### Chrome Not Running with Remote Debugging

If you see an error message about Chrome not running with remote debugging, make sure to start Chrome with the `--remote-debugging-port=9222` flag as described above.

### File Upload Issues

The application tries multiple approaches to upload the image:
1. Direct file input manipulation
2. Alternative upload method using the browse button
3. Manual intervention if automated methods fail

If the automated methods fail, the application will prompt you to manually select the file in the file dialog that appears.

### Translation Timeout

If the translation takes too long, the application will time out after several attempts. You can adjust the timeout settings by modifying the constants in the `ImageTranslator.java` file:
- `MAX_WAIT_ATTEMPTS`: Number of attempts to check if translation is complete
- `WAIT_INTERVAL`: Time to wait between attempts (in milliseconds)

## Customization

### Changing Languages

By default, the application translates from English to Russian. To change the source or target language, modify the `GOOGLE_TRANSLATE_URL` constant in the `ImageTranslator.java` file.

For example, to translate from English to Spanish:
```java
private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/?sl=en&tl=es&op=images";
```

Where:
- `sl=en` specifies English as the source language
- `tl=es` specifies Spanish as the target language

## Advanced Usage

### Integration with Other Applications

You can integrate this application with other systems by calling it from your own scripts or applications. The exit code will be 0 for success and non-zero for failure.

Example of checking the exit code in a shell script:
```bash
./run.sh input.jpg output.jpg
if [ $? -eq 0 ]; then
    echo "Translation successful"
else
    echo "Translation failed"
fi
```

### Batch Processing

To process multiple images, you can create a simple shell script:

```bash
#!/bin/bash
for img in input_folder/*.jpg; do
    output="output_folder/$(basename "$img")"
    ./run.sh "$img" "$output"
    echo "Processed $img -> $output"
done
```
