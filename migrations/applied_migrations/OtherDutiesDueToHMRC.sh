#!/bin/bash

echo ""
echo "Applying migration OtherDutiesDueToHMRC"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /otherDutiesDueToHMRC                        controllers.OtherDutiesDueToHMRCController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /otherDutiesDueToHMRC                        controllers.OtherDutiesDueToHMRCController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOtherDutiesDueToHMRC                  controllers.OtherDutiesDueToHMRCController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOtherDutiesDueToHMRC                  controllers.OtherDutiesDueToHMRCController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "otherDutiesDueToHMRC.title = otherDutiesDueToHMRC" >> ../conf/messages.en
echo "otherDutiesDueToHMRC.heading = otherDutiesDueToHMRC" >> ../conf/messages.en
echo "otherDutiesDueToHMRC.checkYourAnswersLabel = otherDutiesDueToHMRC" >> ../conf/messages.en
echo "otherDutiesDueToHMRC.error.required = Enter otherDutiesDueToHMRC" >> ../conf/messages.en
echo "otherDutiesDueToHMRC.error.length = OtherDutiesDueToHMRC must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryOtherDutiesDueToHMRCUserAnswersEntry: Arbitrary[(OtherDutiesDueToHMRCPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[OtherDutiesDueToHMRCPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryOtherDutiesDueToHMRCPage: Arbitrary[OtherDutiesDueToHMRCPage.type] =";\
    print "    Arbitrary(OtherDutiesDueToHMRCPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(OtherDutiesDueToHMRCPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def otherDutiesDueToHMRC: Option[AnswerRow] = userAnswers.get(OtherDutiesDueToHMRCPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"otherDutiesDueToHMRC.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.OtherDutiesDueToHMRCController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration OtherDutiesDueToHMRC completed"
