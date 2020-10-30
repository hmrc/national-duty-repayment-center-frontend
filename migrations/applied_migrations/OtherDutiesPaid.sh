#!/bin/bash

echo ""
echo "Applying migration OtherDutiesPaid"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /otherDutiesPaid                        controllers.OtherDutiesPaidController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /otherDutiesPaid                        controllers.OtherDutiesPaidController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOtherDutiesPaid                  controllers.OtherDutiesPaidController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOtherDutiesPaid                  controllers.OtherDutiesPaidController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "otherDutiesPaid.title = otherDutiesPaid" >> ../conf/messages.en
echo "otherDutiesPaid.heading = otherDutiesPaid" >> ../conf/messages.en
echo "otherDutiesPaid.checkYourAnswersLabel = otherDutiesPaid" >> ../conf/messages.en
echo "otherDutiesPaid.error.required = Enter otherDutiesPaid" >> ../conf/messages.en
echo "otherDutiesPaid.error.length = OtherDutiesPaid must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryOtherDutiesPaidUserAnswersEntry: Arbitrary[(OtherDutiesPaidPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[OtherDutiesPaidPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryOtherDutiesPaidPage: Arbitrary[OtherDutiesPaidPage.type] =";\
    print "    Arbitrary(OtherDutiesPaidPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(OtherDutiesPaidPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def otherDutiesPaid: Option[AnswerRow] = userAnswers.get(OtherDutiesPaidPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"otherDutiesPaid.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.OtherDutiesPaidController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration OtherDutiesPaid completed"
