

# Welsh translations

## Background
The Welsh translations file (`messages.cy`) is generated from an "English to Welsh Dictionary" stored as a 
tab-separated csv file (`NDRC Welsh Translations.csv`).

The idea is that this dictionary is the "source of truth" and contains the latest message key -> English -> Welsh mappings.

The procedure for updating the dictionary and subsequently the `messages.cy` file is documented below.

## Updating translations

Obtain the new or changed translations from the Welsh Translation team.  These can be in many forms (spreadsheet, table
in Word document) but ultimately for this procedure you will a 'table' of translations in the form - 

| Key               | English text  | Welsh text |
|-----              |-------------- |------------|
| example.heading   |  Phone number | Rhif ffôn  |

Open the dictionary (`NDRC Welsh Translations.csv`) as a tab-delimited file in (say) OpenOffice.

Paste the new message(s) as new rows at the _bottom_ of this file.  Note: if the messages were already translated and
are being updated you do not have to find and update the original entries.   By placing the new translations at the end
of the dictionary they will automatically replace any previous entries as part of the synchronisation process below.

Save the updated dictionary (as tab-delimited csv)

Use the Python script `syncTranslations` (in "utils" folder) to re-generate the `messages.cy` file.

In addition, the script will report if there are missing or changed Welsh translations and produce an output file in
this case which can be used to request new translations from the Welsh team.

For example

```
Missing/changed translations written to ../translations/missing.csv
checkYourAnswers.title  Check your answers before sending your claim
checkYourAnswers.title.hint     Please check your answers
```

You should now be able to see in the git diff of `messages.cy` the new and/or updated entries which should match 
the new translations added to the dictionary at the beginning of this process.

Check in the updated `messages.cy` and dictionary (`NDRC Welsh Translations.csv`)  
