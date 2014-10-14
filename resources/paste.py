import json
import requests
import weechat


weechat.register("paster", "AeroNotix", "0.1", "GPL3", "Paster", "", "")

URL_SERVER = "http://zerolength.com:8080/t/"
URL_SERVER_EXTERNAL = "http://zerolength.com:8080/t/%s/"


def paste(data, buffer, args):
    payload = {
        "text": args
    }
    r = requests.post(URL_SERVER, data=json.dumps(payload),
                      headers={'content-type': 'application/json'})
    if r.status_code == 201:
        weechat.command(buffer, URL_SERVER_EXTERNAL % r.json()['url'])
    return weechat.WEECHAT_RC_OK

hook = weechat.hook_command("paste", "Paste data to ",
                            "",
                            "description of arguments...",
                            "list",
                            "paste",
                            "")
