import requests
from bs4 import BeautifulSoup
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

def create_session(retries, backoff_factor):
    session = requests.Session()
    retry = Retry(
        connect=retries,
        backoff_factor=backoff_factor,
        status_forcelist=[429, 500, 502, 503, 504]
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter=adapter)
    session.mount('https://', adapter=adapter)
    return session

session = create_session(5, 1)
url = "https://mazii.net/vi-VN/search/word/javi/%E5%AD%A6"

r = session.get(url, timeout=10)

# Lưu HTML vào file
with open("kanji_html.html", "w", encoding="utf-8") as f:
    f.write(r.text)

print(f"Status: {r.status_code}")
print(f"Đã lưu HTML vào kanji_html.html")

# Parse và in một số selector để test
soup = BeautifulSoup(r.text, "html.parser")
print(f"\nKanji word (span.japanese-char): {soup.select_one('span.japanese-char').text if soup.select_one('span.japanese-char') else 'Không tìm thấy'}")
print(f"Onyomi (span.txt-on): {[k.text.strip() for k in soup.select('span.txt-on')]}")
print(f"Kunyomi (span.txt-kun): {[k.text.strip() for k in soup.select('span.txt-kun')]}")