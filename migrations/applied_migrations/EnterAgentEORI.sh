#!/bin/bash

echo ""
echo "Applying migration EnterAgentEORI"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /enterAgentEORI                        controllers.EnterAgentEORIController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /enterAgentEORI                        controllers.EnterAgentEORIController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEnterAgentEORI                  controllers.EnterAgentEORIController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEnterAgentEORI                  controllers.EnterAgentEORIController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "enterAgentEORI.title = enterAgentEORI" >> ../conf/messages.en
echo "enterAgentEORI.heading = enterAgentEORI" >> ../conf/messages.en
echo "enterAgentEORI.checkYourAnswersLabel = enterAgentEORI" >> ../conf/messages.en
echo "enterAgentEORI.error.required = Enter enterAgentEORI" >> ../conf/messages.en
echo "enterAgentEORI.error.length = EnterAgentEORI must be 17 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEnterAgentEORIUserAnswersEntry: Arbitrary[(EnterAgentEORIPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EnterAgentEORIPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEnterAgentEORIPage: Arbitrary[EnterAgentEORIPage.type] =";\
    print "    Arbitrary(EnterAgentEORIPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EnterAgentEORIPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def enterAgentEORI: Option[AnswerRow] = userAnswers.get(EnterAgentEORIPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"enterAgentEORI.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.EnterAgentEORIController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration EnterAgentEORI completed"
