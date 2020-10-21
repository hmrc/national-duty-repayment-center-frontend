#!/bin/bash

echo ""
echo "Applying migration CustomsRegulationType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /customsRegulationType                        controllers.CustomsRegulationTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /customsRegulationType                        controllers.CustomsRegulationTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCustomsRegulationType                  controllers.CustomsRegulationTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCustomsRegulationType                  controllers.CustomsRegulationTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customsRegulationType.title = Which regulation are you applying for repayment under?" >> ../conf/messages.en
echo "customsRegulationType.heading = Which regulation are you applying for repayment under?" >> ../conf/messages.en
echo "customsRegulationType.01 = UK Customs Code Regulation (UK) Unknown" >> ../conf/messages.en
echo "customsRegulationType.02 = Unions Customs Code Regulation (EU) 952/2013" >> ../conf/messages.en
echo "customsRegulationType.checkYourAnswersLabel = Which regulation are you applying for repayment under?" >> ../conf/messages.en
echo "customsRegulationType.error.required = Select customsRegulationType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsRegulationTypeUserAnswersEntry: Arbitrary[(CustomsRegulationTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CustomsRegulationTypePage.type]";\
    print "        value <- arbitrary[CustomsRegulationType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsRegulationTypePage: Arbitrary[CustomsRegulationTypePage.type] =";\
    print "    Arbitrary(CustomsRegulationTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsRegulationType: Arbitrary[CustomsRegulationType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(CustomsRegulationType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CustomsRegulationTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def customsRegulationType: Option[AnswerRow] = userAnswers.get(CustomsRegulationTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"customsRegulationType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"customsRegulationType.$x\")),";\
     print "        routes.CustomsRegulationTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration CustomsRegulationType completed"
