import time
import os
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

# Set paths
image_path = "D:\pierre\ingleseGiocando\Primo volume\INGLESE_001.jpg"
download_folder = os.path.expanduser("D:\pierre\downloads")  

# Setup Chrome options
chrome_options = webdriver.ChromeOptions()
prefs = {"download.default_directory": download_folder}
chrome_options.add_experimental_option("prefs", prefs)
chrome_options.add_argument("--start-maximized")

# Start WebDriver
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)


driver.get("https://translate.google.com/?sl=auto&tl=ru&op=images")  

time.sleep(2)  # Wait for page to load

# Find and click the Upload button
upload_button = driver.find_element(By.XPATH, "//input[@type='file']")
upload_button.send_keys(os.path.abspath(image_path))  

time.sleep(5)  # Wait for translation to process

# Take a screenshot of the translated image (alternative to downloading)
translated_image_path = os.path.join(download_folder, "translated_image.png")
driver.save_screenshot(translated_image_path)
print(f"Translated image saved as {translated_image_path}")

# Close the browser
driver.quit()
