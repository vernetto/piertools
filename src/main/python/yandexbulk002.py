import os
import time
from playwright.sync_api import sync_playwright

INPUT_FOLDER = r"D:\temp\images"
OUTPUT_FOLDER = r"D:\temp\images\translated"

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def translate_and_screenshot_all_images():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        context = browser.new_context(viewport={"width": 1280, "height": 900})
        page = context.new_page()
        page.goto("https://translate.yandex.com/en/ocr")
        time.sleep(2)

        for filename in os.listdir(INPUT_FOLDER):
            if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
                image_path = os.path.join(INPUT_FOLDER, filename)
                print(f"Translating: {filename}")

                try:
                    # Upload image
                    input_file = page.locator('input[type="file"]')
                    input_file.set_input_files(image_path)

                    # Wait for translated overlay to appear
                    max_wait = 30  # seconds
                    for i in range(max_wait):
                        try:
                            result_ready = page.locator(".TranslationOcr-TargetText")
                            if result_ready.is_visible() and result_ready.inner_text().strip():
                                break
                        except:
                            pass
                        time.sleep(1)
                    else:
                        print(f"⚠️ Timed out: {filename}")
                        continue

                    # Screenshot only the translated section
                    container = page.locator(".OcrTranslationView-Container")  # Main translated area
                    container.screenshot(path=os.path.join(OUTPUT_FOLDER, filename + "_translated.png"))
                    print(f"✓ Screenshot saved: {filename}_translated.png")

                except Exception as e:
                    print(f"❌ Error for {filename}: {e}")

        browser.close()

translate_and_screenshot_all_images()

