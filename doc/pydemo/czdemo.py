#!/usr/bin/python3
# -*- coding: utf-8 -*-

import sys
import os
import time
import urllib
import urllib.request as request
import lua
import http.cookiejar
import util

if __name__ == '__main__':

    #test lua
    lg = lua.globals()
    lg.dofile('common.lua')
    f=lg.strip
    print(f('\t   sdsa\t '),f)

    key='孩子'
    lg.dofile('2_xindm.lua')

    cfg=[]
    n,t=lg.getconfig()
    for i in range(n):
        cfg.append(t[i+1])
        print(t[i+1])
    file='out.html'

    # search
    key=util.urlencode(key,cfg[4])
    print(key)

    scfg=[]
    n,t=lg.getsearchparam(key)
    for i in range(n):
        scfg.append(t[i+1])
        print(t[i+1])

    if scfg[2] == 'POST':
        res = request.urlopen(scfg[0],scfg[1].encode())
        print(res.status, res.reason)
        s = res.read()
        s = util.convert(s,cfg[1])
        with open(file,'wb') as f:
            f.write(s)

    # getcomic
    comics = []
    n,t=lg.getcomics(file)
    for i in range(n):
        comics.append(t[i+1])
        print(t[i+1])

    url=comics[0].split('||')[3]
    print(url)
    util.geturl(url,file,cfg[1])

    # get parts
    parts = []
    n,t=lg.getparts(file)
    for i in range(n):
        parts.append(t[i+1])
        print(t[i+1])
    # get pics


    #s=request.urlopen('http://www.6manga.com/').read()
    #cookie = http.cookiejar.CookieJar()
    #cjhdr  =  request.HTTPCookieProcessor(cookie)
    #opener = request.build_opener(cjhdr)
    if False:
        url=scfg[0]
        d={"show":"title,btitle","keyboard":key}
        for k,v in d.items():
            d[k]=v.encode(cfg[4])
        postdata = urllib.parse.urlencode(d)
        postdata = postdata.encode()
        print(postdata)
        res = request.urlopen(url,postdata)
        print(res.status, res.reason)
        s = res.read()
        with open('out.html','wb') as f:
            f.write(s)
    if False:
        url='http://www.6manga.com/page/?Search'
        d={'typei':'','type':'','key':key,'submit':'搜尋'}
        for k,v in d.items():
            d[k]=v.encode('big5')
        postdata = urllib.parse.urlencode(d)
        postdata = postdata.encode()
        print(postdata)
        res = request.urlopen(url,postdata)
        print(res.status, res.reason)
        s = res.read()
        with open('out.html','wb') as f:
            f.write(s)


