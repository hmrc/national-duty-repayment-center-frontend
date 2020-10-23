#!/bin/bash

echo ""
echo "Applying migration ImporterName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importerName                        controllers.ImporterNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importerName                        controllers.ImporterNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImporterName                  controllers.ImporterNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImporterName                  controllers.ImporterNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importerName.title = importerName" >> ../conf/messages.en
echo "importerName.heading = importerName" >> ../conf/messages.en
echo "importerName.checkYourAnswersLabel = importerName" >> ../conf/messages.en
echo "importerName.error.required = Enter importerName" >> ../conf/messages.en
echo "importerName.error.length = ImporterName must be 512 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterNameUserAnswersEntry: Arbitrary[(ImporterNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImporterNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImporterNamePage: Arbitrary[ImporterNamePage.type] =";\
    print "    Arbitrary(ImporterNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImporterNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def importerName: Option[AnswerRow] = userAnswers.get(ImporterNamePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"importerName.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ImporterNameController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ImporterName completed"
