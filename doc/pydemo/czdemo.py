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
    print(f('\t   测试lua\t '),f)

    key='女孩'
    #lg.dofile('10_sfacg.lua')
    #lg.dofile('0_kkkmh.lua')
    lg.dofile('2_xindm.lua')
    #lg.dofile('03_6manga.lua')
    #lg.dofile('05_dm5.lua')

    # init network
    cj = http.cookiejar.CookieJar()
    opener = request.build_opener(request.HTTPCookieProcessor(cj))
    request.install_opener(opener)

    cfg=[]
    n,t=lg.getconfig()
    for i in range(n):
        cfg.append(t[i+1])
        print(t[i+1])
    file='out.html'

    # search
    #key1=key.encode(cfg[4])
    #key2=urllib.parse.quote(key1)
    #print(key,key1,key2)
    key=util.urlencode(key,cfg[4])
    key=key.replace('%5C','%')
    print(key)

    scfg=[]
    n,t=lg.getsearchparam(key)
    for i in range(n):
        scfg.append(t[i+1])
        print(t[i+1])

    # navigate
    res = request.urlopen(cfg[5])
    print(res.status, res.reason)
    s = res.read()
    s = util.convert(s,cfg[1])
    with open(file,'wb') as f:
        f.write(s)
    #gethomecomics
    comics = []
    n,t=lg.gethomecomics(file)
    for i in range(n):
        comics.append(t[i+1])
        print(i+1,t[i+1])
    print(comics[0])

    ## search
    #if scfg[2] == 'POST':
    #    res = request.urlopen(scfg[0],scfg[1].encode())
    #elif scfg[2] == 'GET':
    #    res = request.urlopen(scfg[0] + '?' + scfg[1])
    #print(res.status, res.reason)
    #s = res.read()
    #s = util.convert(s,cfg[1])
    #with open(file,'wb') as f:
    #    f.write(s)

    ## getcomic
    #comics = []
    #n,t=lg.getcomics(file)
    #for i in range(n):
    #    comics.append(t[i+1])
    #    print(i+1,t[i+1])

    ## get a comic
    ## TODO i = int(input('choose one comic: ')
    #url=comics[0].split('||')[3]
    #print('use:',url)
    #util.geturl(url,file,cfg[1])

    ## get parts
    #parts = []
    #n,t=lg.getparts(file)
    #for i in range(n):
    #    parts.insert(0,t[i+1])
    #for i in range(n):
    #    print(i+1,parts[i])

    ## get a part
    #url=parts[0].split('||')[1]
    #print('use:',url)
    #util.geturl(url,file,cfg[1])

    ## get pics
    #pics = []
    #n,t=lg.getpics(file)
    #print('pic count:',n)
    #while n == 1:
    #    print(t[1])
    #    util.geturl(t[1],file,cfg[1])
    #    n,t=lg.getpics(file)
    #for i in range(n):
    #    pics.append(t[i+1])
    #    print(i+1,t[i+1])

    #print(cj)
    ## download pics
    #pic='1.jpg'
    #if os.path.exists(pic):
    #    os.remove(pic)
    #print('down pic:',pics[0],'referer:',url)
    #util.geturl(pics[0],pic,referer=url)
    #os.system('feh ' + pic)


