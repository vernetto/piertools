import os
import time
from playwright.sync_api import sync_playwright

INPUT_FOLDER = "D:\\temp\\images"
OUTPUT_FOLDER = "D:\\temp\\images\\translated"

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def translate_image(image_path):
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)  # Set to True to hide the browser
        context = browser.new_context()
        page = context.new_page()
        
        page.goto("https://translate.yandex.com/en/ocr")
        time.sleep(2)

        # Upload image
        input_file = page.locator('input[type="file"]')
        input_file.set_input_files(image_path)
        time.sleep(5)  # Wait for OCR + translation

        # Get translated text
        translated_text = page.locator(".TranslationOcr-TargetText").inner_text()

        browser.close()
        return translated_text

# Process all images
for filename in os.listdir(INPUT_FOLDER):
    if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
        path = os.path.join(INPUT_FOLDER, filename)
        print(f"Translating: {filename}")
        try:
            result = translate_image(path)
            with open(os.path.join(OUTPUT_FOLDER, filename + ".txt"), "w", encoding="utf-8") as f:
                f.write(result)
        except Exception as e:
            print(f"Failed to translate {filename}: {e}")



