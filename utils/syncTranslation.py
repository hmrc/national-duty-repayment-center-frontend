#! /usr/bin/env python

# syncTranslation.py
# outputs Welsh translations in the same order as found in the English message file
# - makes them easier to compare
# - ensures there are no un-used Welsh translations (assuming there are no un-used English messages)

import os

MESSAGE_EN = "../conf/messages.en"
MESSAGE_CY = "../conf/messages.cy"
TRANSLATIONS = "../translations/NDRC Welsh Translations.csv"
MISSING_FILE = "../translations/missing.csv"


translationsMap = dict()

with open(TRANSLATIONS, 'r') as translationsFile:
    translationsLines = translationsFile.readlines()
    for line in translationsLines:
        cols = line.strip().split("\t")
        key = cols[0]
        translationsMap[key] = cols

missingChangedWelsh = []

with open(MESSAGE_EN, 'r') as messages:
    messageLines = messages.readlines()

    with open(MESSAGE_CY, 'w') as outputFile:
        outputFile.write("# GENERATED FILE - DO NOT EDIT\n")
        outputFile.write("# See translations/README.md\n\n")
        for line in messageLines:
            index = line.strip().find("=")
            if index > -1:
                key = line.strip()[0:index].strip()
                value = line.strip()[index+1:].strip()

                if not key in translationsMap or translationsMap[key][1] != value:
                    missingChangedWelsh.append("{}\t{}\n".format(key, value))
                else:
                    outputFile.write("{} = {}\n".format(key, translationsMap[key][2]))
            else:
                outputFile.write(line)

print("Sync'd Welsh messages")
print("--------------------")

if len(missingChangedWelsh) > 0:
    print("Missing/changed translations written to " + MISSING_FILE)
    with open(MISSING_FILE, 'w') as outputFile:
        outputFile.write("Key\tEnglish\n")
        for line in missingChangedWelsh:
            outputFile.write(line)
            print(line, end='')