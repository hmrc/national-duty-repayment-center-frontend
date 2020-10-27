#!/bin/bash

echo ""
echo "Applying migration IsImporterVatRegistered"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /isImporterVatRegistered                        controllers.IsImporterVatRegisteredController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /isImporterVatRegistered                        controllers.IsImporterVatRegisteredController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIsImporterVatRegistered                  controllers.IsImporterVatRegisteredController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIsImporterVatRegistered                  controllers.IsImporterVatRegisteredController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isImporterVatRegistered.title = IsImporterVatRegistered" >> ../conf/messages.en
echo "isImporterVatRegistered.heading = IsImporterVatRegistered" >> ../conf/messages.en
echo "isImporterVatRegistered.yes = Option 1" >> ../conf/messages.en
echo "isImporterVatRegistered.no = Option 2" >> ../conf/messages.en
echo "isImporterVatRegistered.checkYourAnswersLabel = IsImporterVatRegistered" >> ../conf/messages.en
echo "isImporterVatRegistered.error.required = Select isImporterVatRegistered" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsImporterVatRegisteredUserAnswersEntry: Arbitrary[(IsImporterVatRegisteredPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IsImporterVatRegisteredPage.type]";\
    print "        value <- arbitrary[IsImporterVatRegistered].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsImporterVatRegisteredPage: Arbitrary[IsImporterVatRegisteredPage.type] =";\
    print "    Arbitrary(IsImporterVatRegisteredPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsImporterVatRegistered: Arbitrary[IsImporterVatRegistered] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(IsImporterVatRegistered.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IsImporterVatRegisteredPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def isImporterVatRegistered: Option[AnswerRow] = userAnswers.get(IsImporterVatRegisteredPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"isImporterVatRegistered.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"isImporterVatRegistered.$x\")),";\
     print "        routes.IsImporterVatRegisteredController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration IsImporterVatRegistered completed"
