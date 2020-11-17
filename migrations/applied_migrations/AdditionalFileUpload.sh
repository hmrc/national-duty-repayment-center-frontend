#!/bin/bash

echo ""
echo "Applying migration AdditionalFileUpload"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /additionalFileUpload                        controllers.AdditionalFileUploadController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /additionalFileUpload                        controllers.AdditionalFileUploadController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAdditionalFileUpload                  controllers.AdditionalFileUploadController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAdditionalFileUpload                  controllers.AdditionalFileUploadController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "additionalFileUpload.title = AdditionalFileUpload" >> ../conf/messages.en
echo "additionalFileUpload.heading = AdditionalFileUpload" >> ../conf/messages.en
echo "additionalFileUpload.yes = Option 1" >> ../conf/messages.en
echo "additionalFileUpload.no = Option 2" >> ../conf/messages.en
echo "additionalFileUpload.checkYourAnswersLabel = AdditionalFileUpload" >> ../conf/messages.en
echo "additionalFileUpload.error.required = Select additionalFileUpload" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalFileUploadUserAnswersEntry: Arbitrary[(AdditionalFileUploadPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AdditionalFileUploadPage.type]";\
    print "        value <- arbitrary[AdditionalFileUpload].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalFileUploadPage: Arbitrary[AdditionalFileUploadPage.type] =";\
    print "    Arbitrary(AdditionalFileUploadPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalFileUpload: Arbitrary[AdditionalFileUpload] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(AdditionalFileUpload.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AdditionalFileUploadPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def additionalFileUpload: Option[AnswerRow] = userAnswers.get(AdditionalFileUploadPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"additionalFileUpload.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"additionalFileUpload.$x\")),";\
     print "        routes.AdditionalFileUploadController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AdditionalFileUpload completed"
