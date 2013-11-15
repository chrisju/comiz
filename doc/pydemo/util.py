#!/usr/bin/python3
# -*- coding: utf-8 -*-

import sys
import os
import urllib
import urllib.request as request
import http.cookiejar

def urlencode(s,charset):
    print(s.encode(charset))
    return urllib.parse.quote(s.encode(charset))

def convert(bs,charset_src,charset_dst='utf8'):
    return bs.decode(charset_src).encode(charset_dst)

def geturl(url,file,charset=None,referer=None):
    req = urllib.request.Request(url)
    req.add_header('User-Agent', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/30.0.1599.114 Chrome/30.0.1599.114 Safari/537.36')
    if referer != None:
        req.add_header('Referer', referer)
    r = request.urlopen(req)
    s = r.read()
    if charset != None:
        s = convert(s,charset)
    with open(file,'wb') as f:
        f.write(s)

