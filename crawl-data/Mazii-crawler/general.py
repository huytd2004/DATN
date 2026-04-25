import os

# Get current crawler id to continue
def get_continue_id(path: str):
    if not os.path.isfile(path):
        save_current_id(path, 0)
        return 0
    id = None
    with open(path, 'r', encoding='utf-8') as f:
        id = int(f.readline().strip())
    return id

# Save current id for resume crawler
def save_current_id(path: str, current_id: int):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(str(current_id))

# Redirect all error to error.log file
def log_crawler_error(path: str, id: int, word: str, err_type: str):
    with open(path, 'a', encoding='utf-8') as f:
        if 'empty word' in err_type:
            f.write(f"Empty word id: {id}\n")
        if 'no_data' in err_type:
            f.write(f"No data for id: {id}, word: {word}\n")
        if 'exception' in err_type:
            f.write(f"Exception at id: {id}, word: {word}, {err_type}\n")

# Save error link so we can retry later
def error_url_list(path: str, link: str, id: int):
    with open(path, 'a', encoding='utf-8') as f:
        f.write(link + ' ' + str(id) + '\n')