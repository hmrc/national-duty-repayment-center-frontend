#!/bin/bash

echo ""
echo "Applying migration ImporterAddressConfirmation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /importerAddressConfirmation                       controllers.ImporterAddressConfirmationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importerAddressConfirmation.title = importerAddressConfirmation" >> ../conf/messages.en
echo "importerAddressConfirmation.heading = importerAddressConfirmation" >> ../conf/messages.en

echo "Migration ImporterAddressConfirmation completed"
