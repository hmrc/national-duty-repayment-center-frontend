#!/bin/bash

echo ""
echo "Applying migration AgentNameImporter"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /agentNameImporter                        controllers.AgentNameImporterController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /agentNameImporter                        controllers.AgentNameImporterController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAgentNameImporter                  controllers.AgentNameImporterController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAgentNameImporter                  controllers.AgentNameImporterController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agentNameImporter.title = agentNameImporter" >> ../conf/messages.en
echo "agentNameImporter.heading = agentNameImporter" >> ../conf/messages.en
echo "agentNameImporter.checkYourAnswersLabel = agentNameImporter" >> ../conf/messages.en
echo "agentNameImporter.error.required = Enter agentNameImporter" >> ../conf/messages.en
echo "agentNameImporter.error.length = AgentNameImporter must be 512 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentNameImporterUserAnswersEntry: Arbitrary[(AgentNameImporterPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AgentNameImporterPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentNameImporterPage: Arbitrary[AgentNameImporterPage.type] =";\
    print "    Arbitrary(AgentNameImporterPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AgentNameImporterPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def agentNameImporter: Option[AnswerRow] = userAnswers.get(AgentNameImporterPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"agentNameImporter.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.AgentNameImporterController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AgentNameImporter completed"
