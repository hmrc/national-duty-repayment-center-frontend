#!/bin/bash

echo ""
echo "Applying migration IsVatRegistered"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /isVatRegistered                        controllers.IsVatRegisteredController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /isVatRegistered                        controllers.IsVatRegisteredController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIsVatRegistered                  controllers.IsVatRegisteredController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIsVatRegistered                  controllers.IsVatRegisteredController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isVatRegistered.title = isVatRegistered" >> ../conf/messages.en
echo "isVatRegistered.heading = isVatRegistered" >> ../conf/messages.en
echo "isVatRegistered.checkYourAnswersLabel = isVatRegistered" >> ../conf/messages.en
echo "isVatRegistered.error.required = Select yes if isVatRegistered" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsVatRegisteredUserAnswersEntry: Arbitrary[(IsVatRegisteredPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IsVatRegisteredPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsVatRegisteredPage: Arbitrary[IsVatRegisteredPage.type] =";\
    print "    Arbitrary(IsVatRegisteredPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IsVatRegisteredPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def isVatRegistered: Option[AnswerRow] = userAnswers.get(IsVatRegisteredPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"isVatRegistered.checkYourAnswersLabel\")),";\
     print "        yesOrNo(x),";\
     print "        routes.IsVatRegisteredController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration IsVatRegistered completed"
