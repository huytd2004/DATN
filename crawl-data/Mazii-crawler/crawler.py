import requests
from bs4 import BeautifulSoup
from requests.adapters import HTTPAdapter
from requests.sessions import Session
from urllib3.util.retry import Retry


def _first_text(root, selectors):
    for selector in selectors:
        node = root.select_one(selector)
        if node:
            text = node.get_text(strip=True)
            if text:
                return text
    return None


def _first_nonempty_item(root, selectors):
    for selector in selectors:
        for node in root.select(selector):
            text = node.get_text(strip=True)
            if text:
                return text
    return None

def create_session(retries, backoff_factor):
    session = requests.Session()
    retry = Retry(
        connect=retries,
        backoff_factor=backoff_factor,
        status_forcelist=[429, 500, 502, 503, 504] # Retry with these status code
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter=adapter)
    session.mount('https://', adapter=adapter)
    return session


def crawler_word_data(url: str, session: Session):

    r = session.get(url, timeout=10)
    soup = BeautifulSoup(r.text, "html.parser")

    results = None

    for item in soup.select("div.box-main-word"):
        vocab = item.select_one("h3.main-word")
        phonetic = item.select_one("p.phonetic-word")
        han_viet = item.select_one("p.han-viet-word")
        pronunciation = item.select_one("div.txt-pronun")
        type_word = item.select_one("div.type-word")
        meanings = []

        for block in item.select("div.mean-detail-range > div.ng-star-inserted"):
            word_meaning = block.select_one("h4.mean-word")
            if not word_meaning: continue
            word_meaning = word_meaning.get_text(strip=True)

            examples = []
            for detail in block.select("div.item-example"):
                jp = [k.text.strip() for k in detail.select("div.txt-word ruby")]
                vi = detail.select_one("div.example-mean-word span")
                examples.append({
                    "jp": "".join(jp),
                    "vi": vi.text.strip() if vi else None
                })

            meanings.append({
                "meaning": word_meaning,
                "examples": examples
            })


        results = {
            "word": vocab.text.strip() if vocab else None,
            "phonetic": phonetic.text.strip() if phonetic else None,
            "han-viet": han_viet.text.strip() if han_viet else None,
            "pronunciation": pronunciation.text.strip() if pronunciation else None,
            "type": type_word.text.strip() if type_word else None,
            "meaning_detail": meanings
        }

        return results

    return None


def crawl_kanji_data(url: str, session: Session):

    r = session.get(url, timeout=10)
    soup = BeautifulSoup(r.text, "html.parser")

    item = soup.select_one("div.kanji-main-infor") or soup

    kanji = _first_text(item, [
        "p.txt-kanji",
        "span.txt-kanji",
        "span.japanese-char:not(.txt-on):not(.txt-kun)",
    ])

    Kunyomi = [k.get_text(strip=True) for k in item.select("span.txt-kun, span[class*='txt-kun']") if k.get_text(strip=True)]
    Onyomi = [k.get_text(strip=True) for k in item.select("span.txt-on, span[class*='txt-on']") if k.get_text(strip=True)]

    lines = None
    jlpt = None
    meaning = None
    explain = []

    for block in item.select("div.line-item, div[class*='line-item']"):
        title = _first_text(block, ["h4", ".title", "div.title"])
        info = _first_text(block, ["div.item-infor", "div[class*='item-infor']"])
        if not title or not info:
            continue

        title_norm = title.lower()
        if "số nét" in title_norm or "so net" in title_norm or "strokes" in title_norm:
            lines = info
        elif "jlpt" in title_norm:
            jlpt = info
        elif "nghĩa" in title_norm or "meaning" in title_norm:
            meaning = info

    for li in item.select("div.line-item ul.item-infor.show-less li, ul.item-infor li, li.mt-0"):
        text = li.get_text(strip=True)
        if text:
            explain.append(text)

    if not meaning:
        meaning = _first_nonempty_item(item, ["li.mt-0", "ul.item-infor li"])

    results = {
        "word": kanji,
        "Kunyomi": Kunyomi if Kunyomi else None,
        "Onyomi": Onyomi if Onyomi else None,
        "Strokes": lines,
        "JLPT": jlpt,
        "Meaning": meaning,
        "Explain": explain
    }

    if any([results["word"], results["Kunyomi"], results["Onyomi"], results["Meaning"], results["Explain"]]):
        return results
    return None