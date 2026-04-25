from bs4 import BeautifulSoup
import urllib.parse


def link_finder(input: str, output: str):

    with open(input, 'r', encoding='utf-8') as f, \
         open(output, 'w', encoding='utf-8') as out:

        data = f.read()
        soup = BeautifulSoup(data, 'xml')

        prefix = "https://mazii.net/vi-VN/search/"

        for item in soup.find_all('loc'):
            if item.text.startswith(prefix): 
                out.write(item.text + '\n')


def get_word_from_link(link: str):
    word = link.split("javi/")[1]
    return urllib.parse.unquote(word)