#!/bin/bash

echo ""
echo "Applying migration AgentImporterHasEORI"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /agentImporterHasEORI                        controllers.AgentImporterHasEORIController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /agentImporterHasEORI                        controllers.AgentImporterHasEORIController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAgentImporterHasEORI                  controllers.AgentImporterHasEORIController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAgentImporterHasEORI                  controllers.AgentImporterHasEORIController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agentImporterHasEORI.title = Does the importer have an EORI number?" >> ../conf/messages.en
echo "agentImporterHasEORI.heading = Does the importer have an EORI number?" >> ../conf/messages.en
echo "agentImporterHasEORI.yes = Yes" >> ../conf/messages.en
echo "agentImporterHasEORI.no = No" >> ../conf/messages.en
echo "agentImporterHasEORI.checkYourAnswersLabel = Does the importer have an EORI number?" >> ../conf/messages.en
echo "agentImporterHasEORI.error.required = Select agentImporterHasEORI" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentImporterHasEORIUserAnswersEntry: Arbitrary[(AgentImporterHasEORIPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AgentImporterHasEORIPage.type]";\
    print "        value <- arbitrary[AgentImporterHasEORI].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentImporterHasEORIPage: Arbitrary[AgentImporterHasEORIPage.type] =";\
    print "    Arbitrary(AgentImporterHasEORIPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentImporterHasEORI: Arbitrary[AgentImporterHasEORI] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(AgentImporterHasEORI.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AgentImporterHasEORIPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def agentImporterHasEORI: Option[AnswerRow] = userAnswers.get(AgentImporterHasEORIPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"agentImporterHasEORI.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"agentImporterHasEORI.$x\")),";\
     print "        routes.AgentImporterHasEORIController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AgentImporterHasEORI completed"
