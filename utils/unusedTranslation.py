#! /usr/bin/env python

# unusedTranslation.py
# searches for message keys in Welsh that are not in the English file (i.e. have been removed)
# prints out lines that *are* used so that can be used to update the Welsh file

import os

MESSAGE_EN = "../conf/messages.en"
MESSAGE_CY = "../conf/messages.cy"

SOURCE_ROOT = "../app"


englishMap = dict()

englishFile = open(MESSAGE_EN, 'r')
englishLines = englishFile.readlines()
for line in englishLines:
    index = line.strip().find("=")
    key = line.strip()[0:index].strip()
    english = line.strip()[index+1:].strip()
    englishMap[key] = english


def findMissing():

    messages = open(MESSAGE_CY, 'r')
    messageLines = messages.readlines()

    count = 0
    for line in messageLines:
        index = line.strip().find("=")
        if index > -1:
            key = line.strip()[0:index].strip()
            if not key in englishMap:
                count = count + 1
            else:
                print(line, end='')
    return count

print("Used translations")
print("--------------------")


count = findMissing()
print("")
print("--------------------")
print("Found {} unused translations".format(count))


