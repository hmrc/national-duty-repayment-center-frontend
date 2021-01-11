#!/bin/bash

echo ""
echo "Applying migration AmendCaseSendInformation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendCaseSendInformation                        controllers.AmendCaseSendInformationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendCaseSendInformation                        controllers.AmendCaseSendInformationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendCaseSendInformation                  controllers.AmendCaseSendInformationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendCaseSendInformation                  controllers.AmendCaseSendInformationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendCaseSendInformation.title = amendCaseSendInformation" >> ../conf/messages.en
echo "amendCaseSendInformation.heading = amendCaseSendInformation" >> ../conf/messages.en
echo "amendCaseSendInformation.checkYourAnswersLabel = amendCaseSendInformation" >> ../conf/messages.en
echo "amendCaseSendInformation.error.required = Enter amendCaseSendInformation" >> ../conf/messages.en
echo "amendCaseSendInformation.error.length = AmendCaseSendInformation must be 1000 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseSendInformationUserAnswersEntry: Arbitrary[(AmendCaseSendInformationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendCaseSendInformationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseSendInformationPage: Arbitrary[AmendCaseSendInformationPage.type] =";\
    print "    Arbitrary(AmendCaseSendInformationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendCaseSendInformationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def amendCaseSendInformation: Option[AnswerRow] = userAnswers.get(AmendCaseSendInformationPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"amendCaseSendInformation.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.AmendCaseSendInformationController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AmendCaseSendInformation completed"
