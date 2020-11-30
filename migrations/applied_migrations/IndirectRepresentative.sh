#!/bin/bash

echo ""
echo "Applying migration IndirectRepresentative"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /indirectRepresentative                        controllers.IndirectRepresentativeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /indirectRepresentative                        controllers.IndirectRepresentativeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIndirectRepresentative                  controllers.IndirectRepresentativeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIndirectRepresentative                  controllers.IndirectRepresentativeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "indirectRepresentative.title = indirectRepresentative" >> ../conf/messages.en
echo "indirectRepresentative.heading = indirectRepresentative" >> ../conf/messages.en
echo "indirectRepresentative.checkYourAnswersLabel = indirectRepresentative" >> ../conf/messages.en
echo "indirectRepresentative.error.required = Select yes if indirectRepresentative" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIndirectRepresentativeUserAnswersEntry: Arbitrary[(IndirectRepresentativePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IndirectRepresentativePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIndirectRepresentativePage: Arbitrary[IndirectRepresentativePage.type] =";\
    print "    Arbitrary(IndirectRepresentativePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IndirectRepresentativePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def indirectRepresentative: Option[AnswerRow] = userAnswers.get(IndirectRepresentativePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"indirectRepresentative.checkYourAnswersLabel\")),";\
     print "        yesOrNo(x),";\
     print "        routes.IndirectRepresentativeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration IndirectRepresentative completed"
