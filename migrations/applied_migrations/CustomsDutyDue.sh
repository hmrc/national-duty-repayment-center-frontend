#!/bin/bash

echo ""
echo "Applying migration CustomsDutyDue"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /customsDutyDue                        controllers.CustomsDutyDueController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /customsDutyDue                        controllers.CustomsDutyDueController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCustomsDutyDue                  controllers.CustomsDutyDueController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCustomsDutyDue                  controllers.CustomsDutyDueController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customsDutyDue.title = customsDutyDue" >> ../conf/messages.en
echo "customsDutyDue.heading = customsDutyDue" >> ../conf/messages.en
echo "customsDutyDue.checkYourAnswersLabel = customsDutyDue" >> ../conf/messages.en
echo "customsDutyDue.error.required = Enter customsDutyDue" >> ../conf/messages.en
echo "customsDutyDue.error.length = CustomsDutyDue must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsDutyDueUserAnswersEntry: Arbitrary[(CustomsDutyDuePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CustomsDutyDuePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsDutyDuePage: Arbitrary[CustomsDutyDuePage.type] =";\
    print "    Arbitrary(CustomsDutyDuePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CustomsDutyDuePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def customsDutyDue: Option[AnswerRow] = userAnswers.get(CustomsDutyDuePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"customsDutyDue.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.CustomsDutyDueController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration CustomsDutyDue completed"
