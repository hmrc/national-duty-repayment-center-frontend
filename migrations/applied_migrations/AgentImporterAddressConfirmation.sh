#!/bin/bash

echo ""
echo "Applying migration AgentImporterAddressConfirmation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /agentImporterAddressConfirmation                       controllers.AgentImporterAddressConfirmationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agentImporterAddressConfirmation.title = agentImporterAddressConfirmation" >> ../conf/messages.en
echo "agentImporterAddressConfirmation.heading = agentImporterAddressConfirmation" >> ../conf/messages.en

echo "Migration AgentImporterAddressConfirmation completed"
