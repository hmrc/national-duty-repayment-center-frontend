/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers
import play.twirl.api.HtmlFormat
import base.SpecBase
import models.AmendCaseResponseType.{FurtherInformation, SupportingDocuments}
import models.{AmendCaseResponseType, FileUpload, FileUploads, NormalMode, UserAnswers}
import pages.{AmendCaseResponseTypePage, FurtherInformationPage, ReferenceNumberPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat.escape
import services.FileUploaded
import utils.CheckYourAnswersHelper
import views.html.AmendCheckYourAnswersView

import java.time.ZonedDateTime

class AmendCheckYourAnswersControllerSpec extends SpecBase {
  def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  "Amend Check Your Answers Controller" must {

    "return OK and the correct view for a GET when only Documents are selected" in {
      val values: Seq[AmendCaseResponseType] = Seq(SupportingDocuments)

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf"
            )
          )
        ),
        acknowledged = true
      )
      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadedState))
        .set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCheckYourAnswersView]

      status(result) mustEqual OK
      contentAsString(result) must contain
        view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, routes.AmendCaseSendInformationController.showFileUploaded(NormalMode))(fakeRequest, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels mustNot contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels must contain (htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))
      application.stop()
    }

    "return OK and the correct view for a GET when further information is provided" in {
      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)

      val userAnswers = UserAnswers(userAnswersId).set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
        .set(FurtherInformationPage, "hello").success.value

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCheckYourAnswersView]

      status(result) mustEqual OK
      contentAsString(result) must contain
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, routes.FurtherInformationController.onPageLoad(NormalMode))(fakeRequest, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels must contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels mustNot contain (htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))

      application.stop()
    }
    "return OK and the correct view for a GET when both Documents are Further information are selected" in {
      val values: Seq[AmendCaseResponseType] = Seq(SupportingDocuments, FurtherInformation)

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf"
            )
          )
        ),
        acknowledged = true
      )
      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadedState))
        .set(ReferenceNumberPage, "1234").success.value
        .set(FurtherInformationPage, "aaa").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCheckYourAnswersView]

      status(result) mustEqual OK
      contentAsString(result) must contain
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, routes.FurtherInformationController.onPageLoad(NormalMode))(fakeRequest, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels must contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels must contain (htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))
      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
