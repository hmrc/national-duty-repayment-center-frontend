#!/bin/bash

echo ""
echo "Applying migration AmendCaseResponseType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendCaseResponseType                        controllers.AmendCaseResponseTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendCaseResponseType                        controllers.AmendCaseResponseTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendCaseResponseType                  controllers.AmendCaseResponseTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendCaseResponseType                  controllers.AmendCaseResponseTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendCaseResponseType.title = What do you need to do?" >> ../conf/messages.en
echo "amendCaseResponseType.heading = What do you need to do?" >> ../conf/messages.en
echo "amendCaseResponseType.supportingDocuments = furtherInformation" >> ../conf/messages.en
echo "amendCaseResponseType.furtherInformation = Give us further information in writing" >> ../conf/messages.en
echo "amendCaseResponseType.checkYourAnswersLabel = What do you need to do?" >> ../conf/messages.en
echo "amendCaseResponseType.error.required = Select amendCaseResponseType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseResponseTypeUserAnswersEntry: Arbitrary[(AmendCaseResponseTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendCaseResponseTypePage.type]";\
    print "        value <- arbitrary[AmendCaseResponseType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseResponseTypePage: Arbitrary[AmendCaseResponseTypePage.type] =";\
    print "    Arbitrary(AmendCaseResponseTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseResponseType: Arbitrary[AmendCaseResponseType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(AmendCaseResponseType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendCaseResponseTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def amendCaseResponseType: Option[AnswerRow] = userAnswers.get(AmendCaseResponseTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"amendCaseResponseType.checkYourAnswersLabel\")),";\
     print "        Html(x.map(value => HtmlFormat.escape(messages(s\"amendCaseResponseType.$value\")).toString).mkString(\",<br>\")),";\
     print "        routes.AmendCaseResponseTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AmendCaseResponseType completed"
