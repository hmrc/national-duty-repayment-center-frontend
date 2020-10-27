#!/bin/bash

echo ""
echo "Applying migration WhomToPay"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /whomToPay                        controllers.WhomToPayController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /whomToPay                        controllers.WhomToPayController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeWhomToPay                  controllers.WhomToPayController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeWhomToPay                  controllers.WhomToPayController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whomToPay.title = whomToPay" >> ../conf/messages.en
echo "whomToPay.heading = whomToPay" >> ../conf/messages.en
echo "whomToPay.importer = Option 1" >> ../conf/messages.en
echo "whomToPay.representative = Option 2" >> ../conf/messages.en
echo "whomToPay.checkYourAnswersLabel = whomToPay" >> ../conf/messages.en
echo "whomToPay.error.required = Select whomToPay" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhomToPayUserAnswersEntry: Arbitrary[(WhomToPayPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[WhomToPayPage.type]";\
    print "        value <- arbitrary[WhomToPay].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhomToPayPage: Arbitrary[WhomToPayPage.type] =";\
    print "    Arbitrary(WhomToPayPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhomToPay: Arbitrary[WhomToPay] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(WhomToPay.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(WhomToPayPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def whomToPay: Option[AnswerRow] = userAnswers.get(WhomToPayPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"whomToPay.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"whomToPay.$x\")),";\
     print "        routes.WhomToPayController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration WhomToPay completed"
