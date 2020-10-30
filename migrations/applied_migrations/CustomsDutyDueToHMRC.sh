#!/bin/bash

echo ""
echo "Applying migration CustomsDutyDueToHMRC"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /customsDutyDueToHMRC                        controllers.CustomsDutyDueToHMRCController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /customsDutyDueToHMRC                        controllers.CustomsDutyDueToHMRCController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCustomsDutyDueToHMRC                  controllers.CustomsDutyDueToHMRCController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCustomsDutyDueToHMRC                  controllers.CustomsDutyDueToHMRCController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customsDutyDueToHMRC.title = customsDutyDueToHMRC" >> ../conf/messages.en
echo "customsDutyDueToHMRC.heading = customsDutyDueToHMRC" >> ../conf/messages.en
echo "customsDutyDueToHMRC.checkYourAnswersLabel = customsDutyDueToHMRC" >> ../conf/messages.en
echo "customsDutyDueToHMRC.error.required = Enter customsDutyDueToHMRC" >> ../conf/messages.en
echo "customsDutyDueToHMRC.error.length = CustomsDutyDueToHMRC must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsDutyDueToHMRCUserAnswersEntry: Arbitrary[(CustomsDutyDueToHMRCPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CustomsDutyDueToHMRCPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsDutyDueToHMRCPage: Arbitrary[CustomsDutyDueToHMRCPage.type] =";\
    print "    Arbitrary(CustomsDutyDueToHMRCPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CustomsDutyDueToHMRCPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def customsDutyDueToHMRC: Option[AnswerRow] = userAnswers.get(CustomsDutyDueToHMRCPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"customsDutyDueToHMRC.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.CustomsDutyDueToHMRCController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration CustomsDutyDueToHMRC completed"
