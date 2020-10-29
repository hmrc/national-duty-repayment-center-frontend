#!/bin/bash

echo ""
echo "Applying migration VATDueToHMRC"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /vATDueToHMRC                        controllers.VATDueToHMRCController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /vATDueToHMRC                        controllers.VATDueToHMRCController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeVATDueToHMRC                  controllers.VATDueToHMRCController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeVATDueToHMRC                  controllers.VATDueToHMRCController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vATDueToHMRC.title = vATDueToHMRC" >> ../conf/messages.en
echo "vATDueToHMRC.heading = vATDueToHMRC" >> ../conf/messages.en
echo "vATDueToHMRC.checkYourAnswersLabel = vATDueToHMRC" >> ../conf/messages.en
echo "vATDueToHMRC.error.required = Enter vATDueToHMRC" >> ../conf/messages.en
echo "vATDueToHMRC.error.length = VATDueToHMRC must be 14 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVATDueToHMRCUserAnswersEntry: Arbitrary[(VATDueToHMRCPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[VATDueToHMRCPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVATDueToHMRCPage: Arbitrary[VATDueToHMRCPage.type] =";\
    print "    Arbitrary(VATDueToHMRCPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(VATDueToHMRCPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def vATDueToHMRC: Option[AnswerRow] = userAnswers.get(VATDueToHMRCPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"vATDueToHMRC.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.VATDueToHMRCController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration VATDueToHMRC completed"
