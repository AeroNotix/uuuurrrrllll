import weechat
import requests
import re
import json


weechat.register("uuuurrrrllll", "Aaron France", "0.0.1", "GPL",
                 "Uses a local url shortening service", "", "")

weechat.hook_signal("*,irc_in_privmsg", "shorten_url", "")

URL_SERVER = "http://zerolength.com:8080/"
URL_SERVER_EXTERNAL = "http://zerolength.com:8080/%s/"


def irc_message(s):
    nick, command, channel, message = s.split(' ', 3)
    return {
        'nick': nick.split("!")[0][1:],
        'command': command,
        'channel': channel,
        'message': message[1:]
    }


def irc_network(s):
    return s.split(',')[0]


def extract_urls(s):
    return re.findall(
        'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]'
        '|'
        '[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+',
        s
    )


def shorten_url(data, signal, signal_data):
    d = irc_message(signal_data)
    n = irc_network(signal)
    channel_ptr = weechat.buffer_search("irc", "%s.%s" % (n, d['channel']))
    shortened = []
    for url in extract_urls(d['message']):
        if len(URL_SERVER_EXTERNAL) + 6 < len(url):
            continue
        r = requests.post(
            URL_SERVER,
            data=json.dumps({
                'url':     url,
                'nick':    d['nick'],
                'channel': d['channel']
            }),
            headers={'content-type': 'application/json'})
        shortened.append(r.json()['short'])
    if shortened:
        weechat.prnt(channel_ptr,
                     ' | '.join([URL_SERVER_EXTERNAL
                                 % url for url in shortened]))
    return weechat.WEECHAT_RC_OK
