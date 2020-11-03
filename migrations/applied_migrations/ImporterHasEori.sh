#!/bin/bash

echo ""
echo "Applying migration ImporterHasEori"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importerHasEori                        controllers.ImporterHasEoriController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importerHasEori                        controllers.ImporterHasEoriController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImporterHasEori                  controllers.ImporterHasEoriController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImporterHasEori                  controllers.ImporterHasEoriController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importerHasEori.title = importerHasEori" >> ../conf/messages.en
echo "importerHasEori.heading = importerHasEori" >> ../conf/messages.en
echo "importerHasEori.checkYourAnswersLabel = importerHasEori" >> ../conf/messages.en
echo "importerHasEori.error.required = Select yes if importerHasEori" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterHasEoriUserAnswersEntry: Arbitrary[(ImporterHasEoriPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImporterHasEoriPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterHasEoriPage: Arbitrary[ImporterHasEoriPage.type] =";\
    print "    Arbitrary(ImporterHasEoriPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImporterHasEoriPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def importerHasEori: Option[AnswerRow] = userAnswers.get(ImporterHasEoriPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"importerHasEori.checkYourAnswersLabel\")),";\
     print "        yesOrNo(x),";\
     print "        routes.ImporterHasEoriController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ImporterHasEori completed"
