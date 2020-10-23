#!/bin/bash

echo ""
echo "Applying migration WhatAreTheGoods"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /whatAreTheGoods                        controllers.WhatAreTheGoodsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /whatAreTheGoods                        controllers.WhatAreTheGoodsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeWhatAreTheGoods                  controllers.WhatAreTheGoodsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeWhatAreTheGoods                  controllers.WhatAreTheGoodsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatAreTheGoods.title = whatAreTheGoods" >> ../conf/messages.en
echo "whatAreTheGoods.heading = whatAreTheGoods" >> ../conf/messages.en
echo "whatAreTheGoods.checkYourAnswersLabel = whatAreTheGoods" >> ../conf/messages.en
echo "whatAreTheGoods.error.required = Enter whatAreTheGoods" >> ../conf/messages.en
echo "whatAreTheGoods.error.length = WhatAreTheGoods must be 750 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhatAreTheGoodsUserAnswersEntry: Arbitrary[(WhatAreTheGoodsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[WhatAreTheGoodsPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWhatAreTheGoodsPage: Arbitrary[WhatAreTheGoodsPage.type] =";\
    print "    Arbitrary(WhatAreTheGoodsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(WhatAreTheGoodsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def whatAreTheGoods: Option[AnswerRow] = userAnswers.get(WhatAreTheGoodsPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"whatAreTheGoods.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x),";\
     print "        routes.WhatAreTheGoodsController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration WhatAreTheGoods completed"
