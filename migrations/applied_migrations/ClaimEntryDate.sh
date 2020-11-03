#!/bin/bash

echo ""
echo "Applying migration ClaimEntryDate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimEntryDate                  controllers.ClaimEntryDateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimEntryDate                  controllers.ClaimEntryDateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimEntryDate                        controllers.ClaimEntryDateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimEntryDate                        controllers.ClaimEntryDateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimEntryDate.title = ClaimEntryDate" >> ../conf/messages.en
echo "claimEntryDate.heading = ClaimEntryDate" >> ../conf/messages.en
echo "claimEntryDate.checkYourAnswersLabel = ClaimEntryDate" >> ../conf/messages.en
echo "claimEntryDate.error.required.all = Enter the claimEntryDate" >> ../conf/messages.en
echo "claimEntryDate.error.required.two = The claimEntryDate" must include {0} and {1} >> ../conf/messages.en
echo "claimEntryDate.error.required = The claimEntryDate must include {0}" >> ../conf/messages.en
echo "claimEntryDate.error.invalid = Enter a real ClaimEntryDate" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEntryDateUserAnswersEntry: Arbitrary[(ClaimEntryDatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimEntryDatePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEntryDatePage: Arbitrary[ClaimEntryDatePage.type] =";\
    print "    Arbitrary(ClaimEntryDatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimEntryDatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def claimEntryDate: Option[AnswerRow] = userAnswers.get(ClaimEntryDatePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimEntryDate.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x.format(dateFormatter)),";\
     print "        routes.ClaimEntryDateController.onPageLoad(CheckMode).url";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimEntryDate completed"
