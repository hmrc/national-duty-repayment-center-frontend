#!/bin/bash

echo ""
echo "Applying migration ArticleType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /articleType                        controllers.ArticleTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /articleType                        controllers.ArticleTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeArticleType                  controllers.ArticleTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeArticleType                  controllers.ArticleTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "articleType.title = articleType" >> ../conf/messages.en
echo "articleType.heading = articleType" >> ../conf/messages.en
echo "articleType.overchargedAmountsOfImportOrExportDuty = Article 117: Overcharged amounts of import or export duty" >> ../conf/messages.en
echo "articleType.errorByTheCompetentAuthorities = Article 119: Error by the competent authorities" >> ../conf/messages.en
echo "articleType.checkYourAnswersLabel = articleType" >> ../conf/messages.en
echo "articleType.error.required = Select articleType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticleTypeUserAnswersEntry: Arbitrary[(ArticleTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ArticleTypePage.type]";\
    print "        value <- arbitrary[ArticleType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticleTypePage: Arbitrary[ArticleTypePage.type] =";\
    print "    Arbitrary(ArticleTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticleType: Arbitrary[ArticleType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ArticleType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ArticleTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def articleType: Option[AnswerRow] = userAnswers.get(ArticleTypePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"articleType.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"articleType.$x\")),";\
     print "        routes.ArticleTypeController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ArticleType completed"
