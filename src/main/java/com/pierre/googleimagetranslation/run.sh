#!/bin/bash

# Script to run the Image Translator application

# Check if Chrome is running with remote debugging
chrome_debug_port=9222
if ! nc -z localhost $chrome_debug_port &>/dev/null; then
    echo "Chrome is not running with remote debugging enabled on port $chrome_debug_port"
    echo "Please start Chrome with the following command:"
    echo "google-chrome --remote-debugging-port=9222"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &>/dev/null; then
    echo "Maven is not installed. Installing Maven..."
    sudo apt-get update && sudo apt-get install -y maven
fi

# Check if the JAR file exists, if not build it
if [ ! -f "target/image-translator-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "Building the application..."
    mvn clean package
    
    # Check if build was successful
    if [ $? -ne 0 ]; then
        echo "Failed to build the application. Please check the error messages above."
        exit 1
    fi
fi

# Check if arguments are provided
if [ $# -lt 2 ]; then
    echo "Usage: $0 <source-image-path> <output-image-path>"
    echo "Example: $0 test_images/sample.jpg translated_image.jpg"
    exit 1
fi

# Run the application
echo "Running Image Translator..."
java -jar target/image-translator-1.0-SNAPSHOT-jar-with-dependencies.jar "$1" "$2"
