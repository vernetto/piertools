import os
import time
from playwright.sync_api import sync_playwright

INPUT_FOLDER = r"D:\temp\images"
OUTPUT_FOLDER = r"D:\temp\images\translated"

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def translate_all_images():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        context = browser.new_context()
        page = context.new_page()
        page.goto("https://translate.yandex.com/en/ocr")
        time.sleep(2)

        for filename in os.listdir(INPUT_FOLDER):
            if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
                image_path = os.path.join(INPUT_FOLDER, filename)
                print(f"Translating: {filename}")

                try:
                    # Upload new image
                    input_file = page.locator('input[type="file"]')
                    input_file.set_input_files(image_path)

                    # Wait for OCR + translation
                    page.wait_for_selector(".TranslationOcr-TargetText", timeout=15000)
                    time.sleep(1)

                    # Extract translated text
                    translated_text = page.locator(".TranslationOcr-TargetText").inner_text()

                    # Save translation
                    with open(os.path.join(OUTPUT_FOLDER, filename + ".txt"), "w", encoding="utf-8") as f:
                        f.write(translated_text)

                    print(f"✓ Done: {filename}")
                except Exception as e:
                    print(f"❌ Failed to translate {filename}: {e}")

        browser.close()

# Run the improved function
translate_all_images()
