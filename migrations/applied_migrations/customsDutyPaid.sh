#!/bin/bash

echo ""
echo "Applying migration customsDutyPaid"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /customsDutyPaid                        controllers.customsDutyPaidController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /customsDutyPaid                        controllers.customsDutyPaidController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changecustomsDutyPaid                  controllers.customsDutyPaidController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changecustomsDutyPaid                  controllers.customsDutyPaidController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customsDutyPaid.title = customsDutyPaid" >> ../conf/messages.en
echo "customsDutyPaid.heading = customsDutyPaid" >> ../conf/messages.en
echo "customsDutyPaid.checkYourAnswersLabel = customsDutyPaid" >> ../conf/messages.en
echo "customsDutyPaid.error.required = Enter customsDutyPaid" >> ../conf/messages.en
echo "customsDutyPaid.error.length = customsDutyPaid must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarycustomsDutyPaidUserAnswersEntry: Arbitrary[(customsDutyPaidPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[customsDutyPaidPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarycustomsDutyPaidPage: Arbitrary[customsDutyPaidPage.type] =";\
    print "    Arbitrary(customsDutyPaidPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(customsDutyPaidPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def customsDutyPaid: Option[AnswerRow] = userAnswers.get(customsDutyPaidPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"customsDutyPaid.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.customsDutyPaidController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration customsDutyPaid completed"
