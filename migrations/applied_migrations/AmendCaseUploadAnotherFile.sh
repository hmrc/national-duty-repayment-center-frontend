#!/bin/bash

echo ""
echo "Applying migration AmendCaseUploadAnotherFile"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendCaseUploadAnotherFile                        controllers.AmendCaseUploadAnotherFileController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendCaseUploadAnotherFile                        controllers.AmendCaseUploadAnotherFileController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendCaseUploadAnotherFile                  controllers.AmendCaseUploadAnotherFileController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendCaseUploadAnotherFile                  controllers.AmendCaseUploadAnotherFileController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendCaseUploadAnotherFile.title = amendCaseUploadAnotherFile" >> ../conf/messages.en
echo "amendCaseUploadAnotherFile.heading = amendCaseUploadAnotherFile" >> ../conf/messages.en
echo "amendCaseUploadAnotherFile.checkYourAnswersLabel = amendCaseUploadAnotherFile" >> ../conf/messages.en
echo "amendCaseUploadAnotherFile.error.required = Select yes if amendCaseUploadAnotherFile" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseUploadAnotherFileUserAnswersEntry: Arbitrary[(AmendCaseUploadAnotherFilePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendCaseUploadAnotherFilePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendCaseUploadAnotherFilePage: Arbitrary[AmendCaseUploadAnotherFilePage.type] =";\
    print "    Arbitrary(AmendCaseUploadAnotherFilePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendCaseUploadAnotherFilePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def amendCaseUploadAnotherFile: Option[AnswerRow] = userAnswers.get(AmendCaseUploadAnotherFilePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"amendCaseUploadAnotherFile.checkYourAnswersLabel\")),";\
     print "        yesOrNo(x),";\
     print "        routes.AmendCaseUploadAnotherFileController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AmendCaseUploadAnotherFile completed"
