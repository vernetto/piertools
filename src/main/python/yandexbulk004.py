import os
import time
from playwright.sync_api import sync_playwright

INPUT_FOLDER = r"D:\temp\images"
OUTPUT_FOLDER = r"D:\temp\images\translated"

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def translate_and_capture():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        context = browser.new_context(viewport={"width": 1400, "height": 1000})
        page = context.new_page()
        page.goto("https://translate.yandex.com/en/ocr")
        time.sleep(10)

        for filename in os.listdir(INPUT_FOLDER):
            if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
                image_path = os.path.join(INPUT_FOLDER, filename)
                print(f"Uploading: {filename}")

                try:
                    # Upload image
                    input_file = page.locator('input[type="file"]')
                    input_file.set_input_files(image_path)

                    # Wait for any visible text to appear (any element that updates)
                    print("Waiting for translation...")
                    max_wait = 30
                    for i in range(max_wait):
                        if "translated" in page.content().lower() or "translation" in page.content().lower():
                            break
                        time.sleep(1)
                    else:
                        print(f"⚠️ Still no clear translation, but capturing screen anyway.")

                    time.sleep(10)
                    # Screenshot the whole page
                    screenshot_path = os.path.join(OUTPUT_FOLDER, filename + "_RU.png")
                    page.screenshot(path=screenshot_path, full_page=True)
                    print(f"✓ Screenshot saved: {screenshot_path}")

                except Exception as e:
                    print(f"❌ Failed for {filename}: {e}")

        browser.close()

translate_and_capture()
