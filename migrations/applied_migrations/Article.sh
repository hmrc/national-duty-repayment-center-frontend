#!/bin/bash

echo ""
echo "Applying migration Article"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /article                        controllers.ArticleController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /article                        controllers.ArticleController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeArticle                  controllers.ArticleController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeArticle                  controllers.ArticleController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "article.title = Why are you applying for this repayment?" >> ../conf/messages.en
echo "article.heading = Why are you applying for this repayment?" >> ../conf/messages.en
echo "article.overpayment of duty or VAT = Overpayment of duty or VAT" >> ../conf/messages.en
echo "article.error by customs = Error by customs" >> ../conf/messages.en
echo "article.checkYourAnswersLabel = Why are you applying for this repayment?" >> ../conf/messages.en
echo "article.error.required = Select article" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticleUserAnswersEntry: Arbitrary[(ArticlePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ArticlePage.type]";\
    print "        value <- arbitrary[Article].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticlePage: Arbitrary[ArticlePage.type] =";\
    print "    Arbitrary(ArticlePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryArticle: Arbitrary[Article] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(Article.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ArticlePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def article: Option[AnswerRow] = userAnswers.get(ArticlePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"article.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"article.$x\")),";\
     print "        routes.ArticleController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration Article completed"
