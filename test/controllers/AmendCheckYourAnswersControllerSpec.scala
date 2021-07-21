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

import java.time.ZonedDateTime

import base.SpecBase
import models.AmendCaseResponseType.{FurtherInformation, SupportingDocuments}
import models.FileType.SupportingEvidence
import models.responses.ClientClaimResponse
import models.{AmendCaseResponseType, FileUpload, FileUploads, UserAnswers}
import navigation.NavigatorBack
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import pages.{AmendCaseResponseTypePage, FurtherInformationPage, ReferenceNumberPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import queries.AmendClaimIdQuery
import services.FileUploaded
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError
import utils.CheckYourAnswersHelper
import views.html.{AmendCheckYourAnswersView, AmendCheckYourMissingAnswersView}

import scala.concurrent.Future

class AmendCheckYourAnswersControllerSpec extends SpecBase with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  override protected def afterEach(): Unit = {
    reset(mockSessionRepository)
    super.afterEach()
  }

  def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  val backLink = NavigatorBack(Some(routes.ReferenceNumberController.onPageLoad))

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
              "application/pdf",
              Some(SupportingEvidence)
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
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink)(request, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels mustNot contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels must contain(htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))
      application.stop()
    }

    "return OK and the correct view for a GET when further information is provided and clears changePage setting" in {
      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)

      val userAnswers = UserAnswers(userIdentification).set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
        .set(FurtherInformationPage, "hello").success.value
        .copy(changePage = Some(ReferenceNumberPage))

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCheckYourAnswersView]

      status(result) mustEqual OK
      contentAsString(result) must contain
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink)(request, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels must contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels mustNot contain(htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))

      application.stop()

      persistedAnswers.getValue.changePage mustBe None
    }
    "return OK and the correct view for a GET when both Documents and Further information are selected" in {
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
              "application/pdf",
              Some(SupportingEvidence)
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
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink)(request, messages).toString

      val labels = checkYourAnswersHelper.getAmendCheckYourAnswerSections.flatMap(_.rows.map(_.label.toString()))
      labels must contain(htmlEscapedMessage("furtherInformation.checkYourAnswersLabel"))
      labels must contain(htmlEscapedMessage("view.amend-upload-file.checkYourAnswersLabel"))
      application.stop()
    }

    "return OK and the missing answers view for a GET when both Documents and Further information are selected but documents not supplied" in {
      val values: Seq[AmendCaseResponseType] = Seq(SupportingDocuments, FurtherInformation)

      val userAnswers = emptyUserAnswers
        .set(ReferenceNumberPage, "1234").success.value
        .set(FurtherInformationPage, "aaa").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCheckYourMissingAnswersView]

      status(result) mustEqual OK
      contentAsString(result) must contain
      view(checkYourAnswersHelper.getAmendCheckYourAnswerSections, backLink)(request, messages).toString

      application.stop()
    }

    "redirect to missing answers for a GET onResolve when both Documents and Further information are selected but documents not supplied" in {
      val values: Seq[AmendCaseResponseType] = Seq(SupportingDocuments, FurtherInformation)

      val userAnswers = emptyUserAnswers
        .set(ReferenceNumberPage, "1234").success.value
        .set(FurtherInformationPage, "aaa").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onResolve().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AmendCaseSendInformationController.showFileUpload().url

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

    "redirect to start for a GET if amend claim submitted" in {

      when(mockSessionRepository.resetData(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(AmendClaimIdQuery, "1234").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendCheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

      application.stop()
    }

    "redirect to confirmation page when submission is successful" in {

      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)

      val userAnswers = UserAnswers(userIdentification).set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
        .set(FurtherInformationPage, "hello").success.value
        .copy(changePage = Some(ReferenceNumberPage))

      val successResponse = ClientClaimResponse("id", Some("case-id"))

      when(mockClaimService.submitAmendClaim(any())(any(), any())).thenReturn(Future.successful(successResponse))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, routes.AmendCheckYourAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AmendConfirmationController.onPageLoad().url

      application.stop()
    }

    "redirect to error page for when application ref invalid" in {

      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)

      val userAnswers = UserAnswers(userIdentification).set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
        .set(FurtherInformationPage, "hello").success.value
        .copy(changePage = Some(ReferenceNumberPage))

      val errorResponse =
        ClientClaimResponse("id", Some("case-id"), Some(ApiError("code", Some("03- Invalid Case ID"))))

      when(mockClaimService.submitAmendClaim(any())(any(), any())).thenReturn(Future.successful(errorResponse))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, routes.AmendCheckYourAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AmendErrorController.onNotFound().url

      application.stop()
    }

    "redirect to error page for when application ref closed" in {

      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)

      val userAnswers = UserAnswers(userIdentification).set(ReferenceNumberPage, "1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value
        .set(FurtherInformationPage, "hello").success.value
        .copy(changePage = Some(ReferenceNumberPage))

      val errorResponse =
        ClientClaimResponse("id", Some("case-id"), Some(ApiError("code", Some("04 - Requested case already closed"))))

      when(mockClaimService.submitAmendClaim(any())(any(), any())).thenReturn(Future.successful(errorResponse))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, routes.AmendCheckYourAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AmendErrorController.onClosed().url

      application.stop()
    }
  }
}
