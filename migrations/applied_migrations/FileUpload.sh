#!/bin/bash

echo ""
echo "Applying migration FileUpload"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /fileUpload                       controllers.FileUploadController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "fileUpload.title = fileUpload" >> ../conf/messages.en
echo "fileUpload.heading = fileUpload" >> ../conf/messages.en

echo "Migration FileUpload completed"
