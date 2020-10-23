#!/bin/bash

echo ""
echo "Applying migration ReasonForOverpayment"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /reasonForOverpayment                        controllers.ReasonForOverpaymentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /reasonForOverpayment                        controllers.ReasonForOverpaymentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeReasonForOverpayment                  controllers.ReasonForOverpaymentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeReasonForOverpayment                  controllers.ReasonForOverpaymentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "reasonForOverpayment.title = reasonForOverpayment" >> ../conf/messages.en
echo "reasonForOverpayment.heading = reasonForOverpayment" >> ../conf/messages.en
echo "reasonForOverpayment.checkYourAnswersLabel = reasonForOverpayment" >> ../conf/messages.en
echo "reasonForOverpayment.error.required = Enter reasonForOverpayment" >> ../conf/messages.en
echo "reasonForOverpayment.error.length = ReasonForOverpayment must be 750 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryReasonForOverpaymentUserAnswersEntry: Arbitrary[(ReasonForOverpaymentPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ReasonForOverpaymentPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryReasonForOverpaymentPage: Arbitrary[ReasonForOverpaymentPage.type] =";\
    print "    Arbitrary(ReasonForOverpaymentPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ReasonForOverpaymentPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def reasonForOverpayment: Option[AnswerRow] = userAnswers.get(ReasonForOverpaymentPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"reasonForOverpayment.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ReasonForOverpaymentController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ReasonForOverpayment completed"
