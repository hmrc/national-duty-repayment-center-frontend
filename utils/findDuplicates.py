#! /usr/bin/env python

# findDuplicates.py
# finds duplicate entries in messages file
# prints out the evicted (duplicate) entry

import os

MESSAGE_FILE = "../conf/messages.cy"


messageMap = dict()

print("Find duplicates")
print("--------------------")

messageFile = open(MESSAGE_FILE, 'r')
messageLines = messageFile.readlines()
for line in messageLines:
    index = line.strip().find("=")
    key = line.strip()[0:index].strip()
    message = line.strip()[index+1:].strip()
    if key in messageMap and key != '':
        print("{} = {}".format(key, messageMap[key]))
    messageMap[key] = message




print("")
print("--------------------")



