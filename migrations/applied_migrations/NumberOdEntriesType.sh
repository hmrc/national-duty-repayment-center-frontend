#!/bin/bash

echo ""
echo "Applying migration NumberOfEntriesType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /numberOdEntriesType                        controllers.NumberOdEntriesTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /numberOdEntriesType                        controllers.NumberOdEntriesTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeNumberOdEntriesType                  controllers.NumberOdEntriesTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeNumberOdEntriesType                  controllers.NumberOdEntriesTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "numberOdEntriesType.title = How many entries do you want to submit?" >> ../conf/messages.en
echo "numberOdEntriesType.heading = How many entries do you want to submit?" >> ../conf/messages.en
echo "numberOdEntriesType.single = One single entry" >> ../conf/messages.en
echo "numberOdEntriesType.multiple = More than one entry" >> ../conf/messages.en
echo "numberOdEntriesType.checkYourAnswersLabel = How many entries do you want to submit?" >> ../conf/messages.en
echo "numberOdEntriesType.error.required = Select numberOdEntriesType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryNumberOdEntriesTypeUserAnswersEntry: Arbitrary[(NumberOdEntriesTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[NumberOdEntriesTypePage.type]";\
    print "        value <- arbitrary[NumberOdEntriesType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryNumberOdEntriesTypePage: Arbitrary[NumberOdEntriesTypePage.type] =";\
    print "    Arbitrary(NumberOdEntriesTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryNumberOdEntriesType: Arbitrary[NumberOdEntriesType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(NumberOdEntriesType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(NumberOdEntriesTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def numberOdEntriesType: Option[AnswerRow] = userAnswers.get(NumberOdEntriesTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"numberOdEntriesType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"numberOdEntriesType.$x\")),";\
     print "        routes.NumberOdEntriesTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration NumberOdEntriesType completed"
