# Before you execute the Python code, please run chrome:
# "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe  --remote-debugging-port=9222"

# "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"  --remote-debugging-port=9222 -- "%1" 
# and then test http://localhost:9222/json

from selenium import webdriver
from selenium.webdriver.chrome.service import Service

chrome_options = webdriver.ChromeOptions()
chrome_options.debugger_address = "localhost:9222"  

driver = webdriver.Chrome(service=Service(), options=chrome_options)

driver.get("https://translate.google.com/?sl=en&tl=ru&op=images")
print("Connected to existing Chrome session!")


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


