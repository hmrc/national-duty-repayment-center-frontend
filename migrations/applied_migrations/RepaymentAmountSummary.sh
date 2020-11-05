#!/bin/bash

echo ""
echo "Applying migration RepaymentAmountSummary"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /repaymentAmountSummary                       controllers.RepaymentAmountSummaryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "repaymentAmountSummary.title = repaymentAmountSummary" >> ../conf/messages.en
echo "repaymentAmountSummary.heading = repaymentAmountSummary" >> ../conf/messages.en

echo "Migration RepaymentAmountSummary completed"
