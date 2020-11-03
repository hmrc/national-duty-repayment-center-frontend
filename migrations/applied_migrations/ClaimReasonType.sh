#!/bin/bash

echo ""
echo "Applying migration ClaimReasonType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimReasonType                        controllers.ClaimReasonTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimReasonType                        controllers.ClaimReasonTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimReasonType                  controllers.ClaimReasonTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimReasonType                  controllers.ClaimReasonTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimReasonType.title = What does your application relate to?" >> ../conf/messages.en
echo "claimReasonType.heading = What does your application relate to?" >> ../conf/messages.en
echo "claimReasonType.retroActiveQuota = Retro-active quota" >> ../conf/messages.en
echo "claimReasonType.cPUChange = CPC change" >> ../conf/messages.en
echo "claimReasonType.checkYourAnswersLabel = What does your application relate to?" >> ../conf/messages.en
echo "claimReasonType.error.required = Select claimReasonType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimReasonTypeUserAnswersEntry: Arbitrary[(ClaimReasonTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimReasonTypePage.type]";\
    print "        value <- arbitrary[ClaimReasonType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimReasonTypePage: Arbitrary[ClaimReasonTypePage.type] =";\
    print "    Arbitrary(ClaimReasonTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimReasonType: Arbitrary[ClaimReasonType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ClaimReasonType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimReasonTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def claimReasonType: Option[AnswerRow] = userAnswers.get(ClaimReasonTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimReasonType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"claimReasonType.$x\")),";\
     print "        routes.ClaimReasonTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimReasonType completed"
