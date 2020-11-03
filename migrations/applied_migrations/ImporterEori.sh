#!/bin/bash

echo ""
echo "Applying migration ImporterEori"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importerEori                        controllers.ImporterEoriController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importerEori                        controllers.ImporterEoriController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImporterEori                  controllers.ImporterEoriController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImporterEori                  controllers.ImporterEoriController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importerEori.title = importerEori" >> ../conf/messages.en
echo "importerEori.heading = importerEori" >> ../conf/messages.en
echo "importerEori.checkYourAnswersLabel = importerEori" >> ../conf/messages.en
echo "importerEori.error.required = Enter importerEori" >> ../conf/messages.en
echo "importerEori.error.length = ImporterEori must be 17 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterEoriUserAnswersEntry: Arbitrary[(ImporterEoriPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImporterEoriPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterEoriPage: Arbitrary[ImporterEoriPage.type] =";\
    print "    Arbitrary(ImporterEoriPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImporterEoriPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def importerEori: Option[AnswerRow] = userAnswers.get(ImporterEoriPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"importerEori.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ImporterEoriController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ImporterEori completed"
