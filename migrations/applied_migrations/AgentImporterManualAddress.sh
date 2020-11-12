#!/bin/bash

echo ""
echo "Applying migration AgentImporterManualAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /agentImporterManualAddress                        controllers.AgentImporterManualAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /agentImporterManualAddress                        controllers.AgentImporterManualAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAgentImporterManualAddress                  controllers.AgentImporterManualAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAgentImporterManualAddress                  controllers.AgentImporterManualAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agentImporterManualAddress.title = agentImporterManualAddress" >> ../conf/messages.en
echo "agentImporterManualAddress.heading = agentImporterManualAddress" >> ../conf/messages.en
echo "agentImporterManualAddress.checkYourAnswersLabel = agentImporterManualAddress" >> ../conf/messages.en
echo "agentImporterManualAddress.error.required = Enter agentImporterManualAddress" >> ../conf/messages.en
echo "agentImporterManualAddress.error.length = AgentImporterManualAddress must be 128 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentImporterManualAddressUserAnswersEntry: Arbitrary[(AgentImporterManualAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AgentImporterManualAddressPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentImporterManualAddressPage: Arbitrary[AgentImporterManualAddressPage.type] =";\
    print "    Arbitrary(AgentImporterManualAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AgentImporterManualAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def agentImporterManualAddress: Option[AnswerRow] = userAnswers.get(AgentImporterManualAddressPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"agentImporterManualAddress.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.AgentImporterManualAddressController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AgentImporterManualAddress completed"
