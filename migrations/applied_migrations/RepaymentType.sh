#!/bin/bash

echo ""
echo "Applying migration RepaymentType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /repaymentType                        controllers.RepaymentTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /repaymentType                        controllers.RepaymentTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeRepaymentType                  controllers.RepaymentTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeRepaymentType                  controllers.RepaymentTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "repaymentType.title = Select repayment method" >> ../conf/messages.en
echo "repaymentType.heading = Select repayment method" >> ../conf/messages.en
echo "repaymentType.bACS = BACS" >> ../conf/messages.en
echo "repaymentType.cMA = Current month amendment (CMA)" >> ../conf/messages.en
echo "repaymentType.checkYourAnswersLabel = Select repayment method" >> ../conf/messages.en
echo "repaymentType.error.required = Select repaymentType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRepaymentTypeUserAnswersEntry: Arbitrary[(RepaymentTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[RepaymentTypePage.type]";\
    print "        value <- arbitrary[RepaymentType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRepaymentTypePage: Arbitrary[RepaymentTypePage.type] =";\
    print "    Arbitrary(RepaymentTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRepaymentType: Arbitrary[RepaymentType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(RepaymentType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(RepaymentTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def repaymentType: Option[AnswerRow] = userAnswers.get(RepaymentTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"repaymentType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"repaymentType.$x\")),";\
     print "        routes.RepaymentTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration RepaymentType completed"
