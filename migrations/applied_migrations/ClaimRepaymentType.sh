#!/bin/bash

echo ""
echo "Applying migration ClaimRepaymentType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimRepaymentType                        controllers.ClaimRepaymentTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimRepaymentType                        controllers.ClaimRepaymentTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimRepaymentType                  controllers.ClaimRepaymentTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimRepaymentType                  controllers.ClaimRepaymentTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimRepaymentType.title = What are you claiming repayment of?" >> ../conf/messages.en
echo "claimRepaymentType.heading = What are you claiming repayment of?" >> ../conf/messages.en
echo "claimRepaymentType.customs = Customs Duty" >> ../conf/messages.en
echo "claimRepaymentType.vat = VAT" >> ../conf/messages.en
echo "claimRepaymentType.checkYourAnswersLabel = What are you claiming repayment of?" >> ../conf/messages.en
echo "claimRepaymentType.error.required = Select claimRepaymentType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimRepaymentTypeUserAnswersEntry: Arbitrary[(ClaimRepaymentTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimRepaymentTypePage.type]";\
    print "        value <- arbitrary[ClaimRepaymentType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimRepaymentTypePage: Arbitrary[ClaimRepaymentTypePage.type] =";\
    print "    Arbitrary(ClaimRepaymentTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimRepaymentType: Arbitrary[ClaimRepaymentType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ClaimRepaymentType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimRepaymentTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def claimRepaymentType: Option[AnswerRow] = userAnswers.get(ClaimRepaymentTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimRepaymentType.checkYourAnswersLabel\")),";\
     print "        Html(x.map(value => HtmlFormat.escape(messages(s\"claimRepaymentType.$value\")).toString).mkString(\",<br>\")),";\
     print "        routes.ClaimRepaymentTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimRepaymentType completed"
