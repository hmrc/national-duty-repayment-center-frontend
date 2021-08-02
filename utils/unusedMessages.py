#! /usr/bin/env python

# unusedMessages.py
# searches for message keys that are not present in the source code
# exceptions can be added to EXCEPTIONS_FILE in form <key> = <source code sample>

import os

MESSAGE_FILE = "../conf/messages.en"
SOURCE_ROOT = "../app"
EXCEPTIONS_FILE = "unusedMessageExceptions.txt"

def findKey(key):
    rootdir= (SOURCE_ROOT)
    for folder, dirs, files in os.walk(rootdir):
        for file in files:
            if file.endswith('.html') or file.endswith('.scala'):
                fullpath = os.path.join(folder, file)
                with open(fullpath, 'r') as f:
                    for line in f:
                        if key in line:
                            return True
                            break
    return False


exceptionsMap = dict()

exceptionsFile = open(EXCEPTIONS_FILE, 'r')
expLines = exceptionsFile.readlines()
for line in expLines:
    index = line.strip().find("=")
    key = line.strip()[0:index].strip()
    exception = line.strip()[index+1:].strip()
    exceptionsMap[key] = exception

def findException(message):
    if message in exceptionsMap:
        exception = exceptionsMap[message]
        foundExp = findKey(exception)
        if foundExp == False:
            print("WARNING: key found in exceptions file but not in source code: {} = {}".format(message, exception))
        return foundExp
    return False

def findUnused():

    messages = open(MESSAGE_FILE, 'r')
    messageLines = messages.readlines()

    count = 0
    for line in messageLines:
        index = line.strip().find("=")
        key = line.strip()[0:index].strip()
        found = findKey(key)
        if found == False and findException(key) == False:
            count = count + 1
            print(key)
    return count

print("Unused message checker")
print("----------------------")

# test = "articleType.121"
# print("Testing findKey {}: {}".format(test, findKey(test)))
# print("Testing findException {}: {}".format(test, findException(test)))
# print("Testing dictionary {}".format(exceptionsMap))

count = findUnused()
print("Found {} unused keys".format(count))


