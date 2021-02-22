#!/bin/bash

echo ""
echo "Applying migration AmendConfirmation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /amendConfirmation                       controllers.AmendConfirmationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendConfirmation.title = amendConfirmation" >> ../conf/messages.en
echo "amendConfirmation.heading = amendConfirmation" >> ../conf/messages.en

echo "Migration AmendConfirmation completed"
