#! /usr/bin/env python

# syncTranslation.py
# outputs Welsh translations in the same order as found in the English message file
# - makes them easier to compare
# - ensures there are no un-used Welsh translations (assuming there are no un-used English messages)

import os

MESSAGE_EN = "../conf/messages.en"
MESSAGE_CY = "../conf/messages.cy"

SOURCE_ROOT = "../app"


welshMap = dict()

welshFile = open(MESSAGE_CY, 'r')
welshLines = welshFile.readlines()
for line in welshLines:
    index = line.strip().find("=")
    key = line.strip()[0:index].strip()
    welsh = line.strip()[index+1:].strip()
    welshMap[key] = welsh

missingWelsh = []

def process():

    messages = open(MESSAGE_EN, 'r')
    messageLines = messages.readlines()

    count = 0
    for line in messageLines:
        index = line.strip().find("=")
        if index > -1:
            key = line.strip()[0:index].strip()
            if not key in welshMap:
                count = count + 1
                missingWelsh.append(line)
            else:
                print("{} = {}".format(key, welshMap[key]))
        else:
            print(line, end='')
    return count

print("Sync'd Welsh messages")
print("--------------------")


count = process()
print("")
print("--------------------")
print("Found {} missing translations:".format(count))

for line in missingWelsh:
   print(line, end='')


