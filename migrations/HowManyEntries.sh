#!/bin/bash

echo ""
echo "Applying migration HowManyEntries"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /howManyEntries                        controllers.HowManyEntriesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /howManyEntries                        controllers.HowManyEntriesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHowManyEntries                  controllers.HowManyEntriesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHowManyEntries                  controllers.HowManyEntriesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "howManyEntries.title = howManyEntries" >> ../conf/messages.en
echo "howManyEntries.heading = howManyEntries" >> ../conf/messages.en
echo "howManyEntries.checkYourAnswersLabel = howManyEntries" >> ../conf/messages.en
echo "howManyEntries.error.required = Enter howManyEntries" >> ../conf/messages.en
echo "howManyEntries.error.length = HowManyEntries must be 2 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHowManyEntriesUserAnswersEntry: Arbitrary[(HowManyEntriesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HowManyEntriesPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHowManyEntriesPage: Arbitrary[HowManyEntriesPage.type] =";\
    print "    Arbitrary(HowManyEntriesPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HowManyEntriesPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def howManyEntries: Option[AnswerRow] = userAnswers.get(HowManyEntriesPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"howManyEntries.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.HowManyEntriesController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration HowManyEntries completed"
