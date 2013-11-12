#!/usr/bin/python3
# -*- coding: utf-8 -*-

import sys
import os
import urllib

def urlencode(s,charset):
    return urllib.parse.quote(s.encode(charset))

def convert(bs,charset_src,charset_dst='utf8'):
    return bs.decode(charset_src).encode(charset_dst)

def geturl(url,file,charset):
    s = urllib.request.urlopen(url).read()
    s = convert(s,charset)
    with open(file,'wb') as f:
        f.write(s)

