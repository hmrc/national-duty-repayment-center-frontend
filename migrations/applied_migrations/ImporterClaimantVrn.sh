#!/bin/bash

echo ""
echo "Applying migration ImporterClaimantVrn"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importerClaimantVrn                        controllers.ImporterClaimantVrnController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importerClaimantVrn                        controllers.ImporterClaimantVrnController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImporterClaimantVrn                  controllers.ImporterClaimantVrnController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImporterClaimantVrn                  controllers.ImporterClaimantVrnController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importerClaimantVrn.title = importerClaimantVrn" >> ../conf/messages.en
echo "importerClaimantVrn.heading = importerClaimantVrn" >> ../conf/messages.en
echo "importerClaimantVrn.checkYourAnswersLabel = importerClaimantVrn" >> ../conf/messages.en
echo "importerClaimantVrn.error.required = Enter importerClaimantVrn" >> ../conf/messages.en
echo "importerClaimantVrn.error.length = ImporterClaimantVrn must be 9 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterClaimantVrnUserAnswersEntry: Arbitrary[(ImporterClaimantVrnPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImporterClaimantVrnPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterClaimantVrnPage: Arbitrary[ImporterClaimantVrnPage.type] =";\
    print "    Arbitrary(ImporterClaimantVrnPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImporterClaimantVrnPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def importerClaimantVrn: Option[AnswerRow] = userAnswers.get(ImporterClaimantVrnPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"importerClaimantVrn.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ImporterClaimantVrnController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ImporterClaimantVrn completed"
