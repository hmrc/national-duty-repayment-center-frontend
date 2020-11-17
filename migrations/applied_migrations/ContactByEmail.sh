#!/bin/bash

echo ""
echo "Applying migration ContactByEmail"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /contactByEmail                        controllers.ContactByEmailController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /contactByEmail                        controllers.ContactByEmailController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeContactByEmail                  controllers.ContactByEmailController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeContactByEmail                  controllers.ContactByEmailController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "contactByEmail.title = ContactByEmail" >> ../conf/messages.en
echo "contactByEmail.heading = ContactByEmail" >> ../conf/messages.en
echo "contactByEmail.yes = Option 1" >> ../conf/messages.en
echo "contactByEmail.no = Option 2" >> ../conf/messages.en
echo "contactByEmail.checkYourAnswersLabel = ContactByEmail" >> ../conf/messages.en
echo "contactByEmail.error.required = Select contactByEmail" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContactByEmailUserAnswersEntry: Arbitrary[(ContactByEmailPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ContactByEmailPage.type]";\
    print "        value <- arbitrary[ContactByEmail].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContactByEmailPage: Arbitrary[ContactByEmailPage.type] =";\
    print "    Arbitrary(ContactByEmailPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContactByEmail: Arbitrary[ContactByEmail] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ContactByEmail.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ContactByEmailPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def contactByEmail: Option[AnswerRow] = userAnswers.get(ContactByEmailPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"contactByEmail.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(messages(s\"contactByEmail.$x\")),";\
     print "        routes.ContactByEmailController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ContactByEmail completed"
