#!/bin/bash

echo ""
echo "Applying migration ClaimEpu"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /claimEpu                        controllers.ClaimEpuController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /claimEpu                        controllers.ClaimEpuController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeClaimEpu                  controllers.ClaimEpuController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeClaimEpu                  controllers.ClaimEpuController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "claimEpu.title = claimEpu" >> ../conf/messages.en
echo "claimEpu.heading = claimEpu" >> ../conf/messages.en
echo "claimEpu.checkYourAnswersLabel = claimEpu" >> ../conf/messages.en
echo "claimEpu.error.required = Enter claimEpu" >> ../conf/messages.en
echo "claimEpu.error.length = ClaimEpu must be 3 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEpuUserAnswersEntry: Arbitrary[(ClaimEpuPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ClaimEpuPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryClaimEpuPage: Arbitrary[ClaimEpuPage.type] =";\
    print "    Arbitrary(ClaimEpuPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ClaimEpuPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def claimEpu: Option[AnswerRow] = userAnswers.get(ClaimEpuPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"claimEpu.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.ClaimEpuController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ClaimEpu completed"
