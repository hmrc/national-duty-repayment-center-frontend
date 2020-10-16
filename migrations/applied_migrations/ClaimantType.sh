#!/bin/bash

echo ""
echo "Applying migration ClaimantType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimantType                        controllers.ClaimantTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimantType                        controllers.ClaimantTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimantType                  controllers.ClaimantTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimantType                  controllers.ClaimantTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimantType.title = Are you the importer or their representative?" >> ../conf/messages.en
echo "claimantType.heading = Are you the importer or their representative?" >> ../conf/messages.en
echo "claimantType.importer = I am the importer" >> ../conf/messages.en
echo "claimantType.representative = I am a representative of the importer" >> ../conf/messages.en
echo "claimantType.checkYourAnswersLabel = Are you the importer or their representative?" >> ../conf/messages.en
echo "claimantType.error.required = Select claimantType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimantTypeUserAnswersEntry: Arbitrary[(ClaimantTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimantTypePage.type]";\
    print "        value <- arbitrary[ClaimantType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimantTypePage: Arbitrary[ClaimantTypePage.type] =";\
    print "    Arbitrary(ClaimantTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimantType: Arbitrary[ClaimantType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ClaimantType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimantTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def claimantType: Option[AnswerRow] = userAnswers.get(ClaimantTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimantType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"claimantType.$x\")),";\
     print "        routes.ClaimantTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimantType completed"
