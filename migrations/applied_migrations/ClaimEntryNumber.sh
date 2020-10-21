#!/bin/bash

echo ""
echo "Applying migration ClaimEntryNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimEntryNumber                        controllers.ClaimEntryNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimEntryNumber                        controllers.ClaimEntryNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimEntryNumber                  controllers.ClaimEntryNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimEntryNumber                  controllers.ClaimEntryNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimEntryNumber.title = claimEntryNumber" >> ../conf/messages.en
echo "claimEntryNumber.heading = claimEntryNumber" >> ../conf/messages.en
echo "claimEntryNumber.checkYourAnswersLabel = claimEntryNumber" >> ../conf/messages.en
echo "claimEntryNumber.error.required = Enter claimEntryNumber" >> ../conf/messages.en
echo "claimEntryNumber.error.length = ClaimEntryNumber must be 7 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEntryNumberUserAnswersEntry: Arbitrary[(ClaimEntryNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimEntryNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEntryNumberPage: Arbitrary[ClaimEntryNumberPage.type] =";\
    print "    Arbitrary(ClaimEntryNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimEntryNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def claimEntryNumber: Option[AnswerRow] = userAnswers.get(ClaimEntryNumberPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimEntryNumber.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ClaimEntryNumberController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimEntryNumber completed"
