import weechat


weechat.register("uuuurrrrllll", "Aaron France", "0.0.1", "GPL",
                 "Uses a local url shortening service", "", "")

weechat.hook_signal("*,irc_in_privmsg", "shorten_url", "")


def shorten_url(data, signal, signal_data):
    weechat.prn(str(data))
    weechat.prn(str(signal))
    weechat.prn(str(signal_data))                
    return weechat.WEECHAT_RC_OK
