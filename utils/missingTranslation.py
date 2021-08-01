#! /usr/bin/env python

# missingTranslation.py
# searches for message keys that do not have a corresponding translation

import os

MESSAGE_EN = "../conf/messages.en"
MESSAGE_CY = "../conf/messages.cy"

SOURCE_ROOT = "../app"
EXCEPTIONS_FILE = "unusedMessageExceptions.txt"



translationsMap = dict()

translationsFile = open(MESSAGE_CY, 'r')
translationLines = translationsFile.readlines()
for line in translationLines:
    index = line.strip().find("=")
    key = line.strip()[0:index].strip()
    translation = line.strip()[index+1:].strip()
    translationsMap[key] = translation


def findMissing():

    messages = open(MESSAGE_EN, 'r')
    messageLines = messages.readlines()

    count = 0
    for line in messageLines:
        index = line.strip().find("=")
        if index > -1:
            key = line.strip()[0:index].strip()
            if not key in translationsMap:
                count = count + 1
                print(key)
    return count

print("Missing translations")
print("--------------------")


count = findMissing()
print("Found {} missing translations".format(count))


