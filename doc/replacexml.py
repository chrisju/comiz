#!/usr/bin/python
# coding=utf8

import re
import sys

def getstrings(f):

    ss = re.findall('>(.*)<',open(f).read())
    outf=open('out','w')
    for s in ss:
        outf.write(s+'\n')
    print 'out created!'

def putstrings(f):

    ss=[]
    inf=open('in')
    for line in open(f).readlines():
        if re.search('>(.*)<',line):
            s=inf.readline()[:-1]
            ss.append(re.sub(r'>(.*)<',r'>\1 '+s+'<',line))
        else:
            ss.append(line)
    outf=open(f,'w')
    for s in ss:
        outf.write(s)
    print 'ok!'

def putstrings2(f):

    ss=[]
    inf=open('in')
    for line in open(f).readlines():
        if re.search('>(.*)<',line):
            s=inf.readline()[:-1]
            ss.append(re.sub(r'>(.*)<','>'+s+'<',line))
        else:
            ss.append(line)
    outf=open(f,'w')
    for s in ss:
        outf.write(s)
    print 'ok!'

if __name__ == '__main__':

    if sys.argv[1]=='out':
        getstrings(sys.argv[2])
    elif sys.argv[1]=='in':
        putstrings(sys.argv[2])
    elif sys.argv[1]=='in2':
        putstrings2(sys.argv[2])
    else:
        print 'usage:\nreplacexml [out|in|in2] xxx.xml'
