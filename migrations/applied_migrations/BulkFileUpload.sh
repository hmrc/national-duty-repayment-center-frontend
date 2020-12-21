#!/bin/bash

echo ""
echo "Applying migration BulkFileUpload"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /bulkFileUpload                        controllers.BulkFileUploadController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /bulkFileUpload                        controllers.BulkFileUploadController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBulkFileUpload                  controllers.BulkFileUploadController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBulkFileUpload                  controllers.BulkFileUploadController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "bulkFileUpload.title = Upload the multiple entries spreadsheet" >> ../conf/messages.en
echo "bulkFileUpload.heading = Upload the multiple entries spreadsheet" >> ../conf/messages.en
echo "bulkFileUpload.the entry processing unit (EPU), entry number and entry date = Option 1" >> ../conf/messages.en
echo "bulkFileUpload.all item numbers = Option 2" >> ../conf/messages.en
echo "bulkFileUpload.checkYourAnswersLabel = Upload the multiple entries spreadsheet" >> ../conf/messages.en
echo "bulkFileUpload.error.required = Select bulkFileUpload" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBulkFileUploadUserAnswersEntry: Arbitrary[(BulkFileUploadPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BulkFileUploadPage.type]";\
    print "        value <- arbitrary[BulkFileUpload].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBulkFileUploadPage: Arbitrary[BulkFileUploadPage.type] =";\
    print "    Arbitrary(BulkFileUploadPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBulkFileUpload: Arbitrary[BulkFileUpload] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(BulkFileUpload.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BulkFileUploadPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def bulkFileUpload: Option[AnswerRow] = userAnswers.get(BulkFileUploadPage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"bulkFileUpload.checkYourAnswersLabel\")),";\
     print "        Html(x.map(value => HtmlFormat.escape(messages(s\"bulkFileUpload.$value\")).toString).mkString(\",<br>\")),";\
     print "        routes.BulkFileUploadController.onPageLoad(CheckMode).url";\
     print "      )"
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration BulkFileUpload completed"
