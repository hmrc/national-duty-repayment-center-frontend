

# national-duty-repayment-center-frontend

## About
The frontend to "Apply for repayment of import duty and import VAT"

National Duty Repayment Centre (NDRC) is for the repayments of import charges which may arise when post clearance amendments are made or imported goods are returned or rejected.
Previously users would fill out an C285 form and submit as a PDF with the relevant documentation and email it to the National Duty Repayment Centre email address. Now they can use the service to complete an online C285 form, attach the necessary documentation and to submit it the Duty Repayment Centre for processing.

Other related NDRC services:
- Backend service: [National-Duty-Repayment-Center](https://github.com/hmrc/national-duty-repayment-center)
- Stubs: [National-Duty-Repayment-Center-Stubs](https://github.com/hmrc/national-duty-repayment-center-stubs)

## User journey

The landing page of the service allows users to select if they would like to submit a new claim (create claim) or add additional information to an existing claim (amend claim).

The 'create claim' user journey allows the user to make a new claim. The journey will take the user through a number of pages to collect all the required data, followed by a declaration page, after which the data is sent to the backend which then sends the data through EIS to PEGA.

There are two major variations on the 'create claim' journey: one for importers and one for representatives. The latter captures data for both the representative and the importer.

Another variation worth noting is the option for the claimant to make a single claim or bulk claim. Bulk claims require the user to upload a spreadsheet with information relevant to each claim.

The 'amend claim' journey allows the user to provide further information about the claim or upload further supporting evidence.

At the end of each user journey the user will be presented with their answers for them to check. They will be given the opportunity here to change any of their answers before submitting.

If the submission is successful the user will be presented with a claim reference number. This reference number is generated in PEGA and returned to the front-end service.

## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/national-duty-repayment-center-frontend.git`

Run the code from source using 

`sbt run`

Running the code from source to test acceptance tests using

`sbt run 

Dependencies will also need to be started from source or using service manager.

## Running through service manager

*You need to be on the VPN*

Ensure your service manager config is up to date, and run the following command:

`sm2 --start NDRC_ALL`

This will start all the required services

## Using the service through auth-login-stub

Open your browser and navigate to the following url:

`http://localhost:9949/auth-login-stub/gg-sign-in` and enter `http://localhost:8450/apply-for-repayment-of-import-duty-and-import-vat` in the Redirect URL field.


## Technical information

The service uses MongoDB to store user session answers.

## Welsh translations
Welsh translations are enabled for an environment by ensuring that `cy` is present in the following `application.conf`
setting

```
play.i18n.langs = ["en", "cy"]
```

The process for maintaining the Welsh translation file is described [here](translations/README.md). 

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
