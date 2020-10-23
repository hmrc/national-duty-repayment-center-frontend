#!/bin/bash

echo ""
echo "Applying migration EvidenceSupportingDocs"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /evidenceSupportingDocs                        controllers.EvidenceSupportingDocsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /evidenceSupportingDocs                        controllers.EvidenceSupportingDocsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEvidenceSupportingDocs                  controllers.EvidenceSupportingDocsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEvidenceSupportingDocs                  controllers.EvidenceSupportingDocsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "evidenceSupportingDocs.title = What evidence do you have to support the application?" >> ../conf/messages.en
echo "evidenceSupportingDocs.heading = What evidence do you have to support the application?" >> ../conf/messages.en
echo "evidenceSupportingDocs.invoice = Option 1" >> ../conf/messages.en
echo "evidenceSupportingDocs.option2 = TransportDocuments" >> ../conf/messages.en
echo "evidenceSupportingDocs.checkYourAnswersLabel = What evidence do you have to support the application?" >> ../conf/messages.en
echo "evidenceSupportingDocs.error.required = Select evidenceSupportingDocs" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEvidenceSupportingDocsUserAnswersEntry: Arbitrary[(EvidenceSupportingDocsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EvidenceSupportingDocsPage.type]";\
    print "        value <- arbitrary[EvidenceSupportingDocs].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEvidenceSupportingDocsPage: Arbitrary[EvidenceSupportingDocsPage.type] =";\
    print "    Arbitrary(EvidenceSupportingDocsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEvidenceSupportingDocs: Arbitrary[EvidenceSupportingDocs] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(EvidenceSupportingDocs.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EvidenceSupportingDocsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def evidenceSupportingDocs: Option[AnswerRow] = userAnswers.get(EvidenceSupportingDocsPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"evidenceSupportingDocs.checkYourAnswersLabel\")),";\
     print "        Html(x.map(value => HtmlFormat.escape(messages(s\"evidenceSupportingDocs.$value\")).toString).mkString(\",<br>\")),";\
     print "        routes.EvidenceSupportingDocsController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration EvidenceSupportingDocs completed"
