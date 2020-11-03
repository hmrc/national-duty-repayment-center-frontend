#!/bin/bash

echo ""
echo "Applying migration TypeOfRepayment"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /typeOfRepayment                        controllers.TypeOfRepaymentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /typeOfRepayment                        controllers.TypeOfRepaymentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTypeOfRepayment                  controllers.TypeOfRepaymentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTypeOfRepayment                  controllers.TypeOfRepaymentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "typeOfRepayment.title = What was overpaid?" >> ../conf/messages.en
echo "typeOfRepayment.heading = What was overpaid?" >> ../conf/messages.en
echo "typeOfRepayment.customs Duty = Option 1" >> ../conf/messages.en
echo "typeOfRepayment.vAT = Option 2" >> ../conf/messages.en
echo "typeOfRepayment.checkYourAnswersLabel = What was overpaid?" >> ../conf/messages.en
echo "typeOfRepayment.error.required = Select typeOfRepayment" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTypeOfRepaymentUserAnswersEntry: Arbitrary[(TypeOfRepaymentPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TypeOfRepaymentPage.type]";\
    print "        value <- arbitrary[TypeOfRepayment].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTypeOfRepaymentPage: Arbitrary[TypeOfRepaymentPage.type] =";\
    print "    Arbitrary(TypeOfRepaymentPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTypeOfRepayment: Arbitrary[TypeOfRepayment] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(TypeOfRepayment.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TypeOfRepaymentPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def typeOfRepayment: Option[AnswerRow] = userAnswers.get(TypeOfRepaymentPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"typeOfRepayment.checkYourAnswersLabel\")),";\
     print "        Html(x.map(value => HtmlFormat.escape(messages(s\"typeOfRepayment.$value\")).toString).mkString(\",<br>\")),";\
     print "        routes.TypeOfRepaymentController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TypeOfRepayment completed"
