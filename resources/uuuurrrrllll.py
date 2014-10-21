import weechat
import requests
import re
import json


weechat.register("uuuurrrrllll", "Aaron France", "0.0.1", "GPL",
                 "Uses a local url shortening service", "", "")

weechat.hook_print('', 'irc_privmsg', '', 1, 'shorten_url', '')

URL_SERVER = "http://zerolength.com:8080/"
URL_SERVER_EXTERNAL = "http://zerolength.com:8080/%s/"


def extract_urls(s):
    return re.findall(
        'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]'
        '|'
        '[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+',
        s
    )


def shorten_url(data, buf, date, tags, displayed, hilight, prefix, msg):
    channel = weechat.buffer_get_string(buf, "name").split(".")[1]
    shortened = []
    my_nick = weechat.buffer_get_string(buf, 'localvar_nick')
    if prefix == my_nick:
        return weechat.WEECHAT_RC_OK
    for url in extract_urls(msg):
        if len(URL_SERVER_EXTERNAL) + 6 > len(url):
            continue
        r = requests.post(
            URL_SERVER,
            data=json.dumps({
                'url':     url,
                'nick':    prefix,
                'channel': channel
            }),
            headers={'content-type': 'application/json'})
        shortened.append(r.json()['short'])
    if shortened:
        weechat.prnt(buf, ' | '.join([URL_SERVER_EXTERNAL
                                      % url for url in shortened]))
    return weechat.WEECHAT_RC_OK
