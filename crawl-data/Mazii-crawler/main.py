from link_finder import link_finder, get_word_from_link
from general import get_continue_id, save_current_id, log_crawler_error, error_url_list
from crawler import create_session, crawler_word_data, crawl_kanji_data
import time, random, json
from multiprocessing import Process

def sleep_to_avoid_ban(req_id: int):
    time.sleep(random.uniform(0.5, 1.5))
    if req_id % 50 == 0:
        time.sleep(random.uniform(7, 10))

def main(args, crawl_type = None):

    XML_FILE, VOCAB_FILE, OUTPUT_FILE, SAVE_FILE, ERROR_FILE, ERROR_LINK = args
    link_finder(XML_FILE, VOCAB_FILE)

    RETRY_CONNECT = 5
    RETRY_BACKOFF_FACTOR = 1
    session = create_session(RETRY_CONNECT, RETRY_BACKOFF_FACTOR)

    with open(VOCAB_FILE, 'r', encoding='utf-8') as f, \
         open(OUTPUT_FILE, 'a', encoding='utf-8') as output:

        lines = f.readlines()
        id = get_continue_id(SAVE_FILE)

        for row in lines[id:]:
            id += 1
            sleep_to_avoid_ban(id)

            link = row.strip()
            word = get_word_from_link(link)
            if not word:
                log_crawler_error(ERROR_FILE, id, word, 'empty word')
                error_url_list(ERROR_LINK, link, id)
                continue

            # Crawl data
            data = None
            for attempt in range(5):
                try:
                    if crawl_type == 'word':
                        data = crawler_word_data(link, session)
                    elif crawl_type == 'kanji':
                        data = crawl_kanji_data(link, session)
                except Exception as e:
                    log_crawler_error(ERROR_FILE, id, word, f"attempt {attempt+1} exception: {e}")
                    time.sleep(2)
                if data: break

            if not data:    
                log_crawler_error(ERROR_FILE, id, word, 'no_data')
                error_url_list(ERROR_LINK, link, id)
                continue

            # Save data and id
            record = {"id": str(id), "data": data}
            output.write(json.dumps(record, ensure_ascii=False) + "\n")
            output.flush()

            save_current_id(SAVE_FILE, id)


if __name__ == "__main__":
    thread_1 = (["crawl_vocab_1/dict-word-1.xml","crawl_vocab_1/vocab1.txt","crawl_vocab_1/vocab1.ndjson","crawl_vocab_1/save.txt","crawl_vocab_1/error.log","crawl_vocab_1/error_link.txt"], 'word')
    thread_2 = (["crawl_vocab_2/dict-word-2.xml","crawl_vocab_2/vocab2.txt","crawl_vocab_2/vocab2.ndjson","crawl_vocab_2/save.txt","crawl_vocab_2/error.log","crawl_vocab_2/error_link.txt"], 'word')
    thread_3 = (["crawl_vocab_3/dict-word-3.xml","crawl_vocab_3/vocab3.txt","crawl_vocab_3/vocab3.ndjson","crawl_vocab_3/save.txt","crawl_vocab_3/error.log","crawl_vocab_3/error_link.txt"], 'word')
    thread_4 = (["crawl_vocab_4/dict-word-4.xml","crawl_vocab_4/vocab4.txt","crawl_vocab_4/vocab4.ndjson","crawl_vocab_4/save.txt","crawl_vocab_4/error.log","crawl_vocab_4/error_link.txt"], 'word')
    thread_5 = (["crawl_vocab_5/dict-word-5.xml","crawl_vocab_5/vocab5.txt","crawl_vocab_5/vocab5.ndjson","crawl_vocab_5/save.txt","crawl_vocab_5/error.log","crawl_vocab_5/error_link.txt"], 'word')
    thread_6 = (["crawl_vocab_6/dict-word-6.xml","crawl_vocab_6/vocab6.txt","crawl_vocab_6/vocab6.ndjson","crawl_vocab_6/save.txt","crawl_vocab_6/error.log","crawl_vocab_6/error_link.txt"], 'word')
    thread_7 = (["crawl_kanji/dict-kanji-1.xml","crawl_kanji/kanji.txt","crawl_kanji/kanji.ndjson","crawl_kanji/save.txt","crawl_kanji/error.log","crawl_kanji/error_link.txt"], 'kanji')


    crawlers = [
        # Process(target=main, args=thread_1),
        # Process(target=main, args=thread_2),
        # Process(target=main, args=thread_3),
        # Process(target=main, args=thread_4),
        # Process(target=main, args=thread_5),
        # Process(target=main, args=thread_6),
        Process(target=main, args=thread_7)
    ]
    for c in crawlers:
        c.start()
    for c in crawlers:
        c.join()

    