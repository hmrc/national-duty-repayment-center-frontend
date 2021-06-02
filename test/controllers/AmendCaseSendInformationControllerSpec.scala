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

import base.SpecBase
import com.kenshoo.play.metrics.{Metrics, MetricsImpl}
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest, UpscanInitiateResponse}
import controllers.actions._
import models.FileType.SupportingEvidence
import models.requests.UploadRequest
import models.{
  AgentImporterHasEORI,
  AmendCaseResponseType,
  CheckMode,
  FileUpload,
  FileUploads,
  NormalMode,
  SessionState,
  UpscanNotification,
  UserAnswers
}
import org.mockito.Matchers.{any, anyObject}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentImporterHasEORIPage, AmendCaseResponseTypePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.FileUploaded
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}

class AmendCaseSendInformationControllerSpec extends SpecBase with MockitoSugar {
  val id = "1"

  "GET /file-upload" should {
    "show the upload first document page" in {
      val fileUploadUrl = routes.AmendCaseSendInformationController.showFileUpload(NormalMode).url
      val application   = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
          Future.successful(SessionState(None, Some(emptyUserAnswers)))
        )
        when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)
        val request = buildRequest(GET, fileUploadUrl)
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include(htmlEscapedMessage("view.amend-upload-file.heading"))
      }
      application.stop()
    }
  }

  "GET /file-uploaded" should {
    "show file uploaded page" in {
      val fileUploadedUrl = routes.AmendCaseSendInformationController.showFileUploaded(NormalMode).url

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
      val amendCaseResponseType: Set[AmendCaseResponseType] =
        Set(AmendCaseResponseType.FurtherInformation, AmendCaseResponseType.SupportingDocuments)
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()
      running(application) {
        when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
          Future.successful(SessionState(None, Some(userAnswers)))
        )
        when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

        val request = buildRequest(GET, fileUploadedUrl)
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include("You have uploaded 1 file")
      }
      application.stop()
    }
  }

  "POST /file-uploaded" should {
    "go to Further information page" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

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
      val amendCaseResponseType: Set[AmendCaseResponseType] =
        Set(AmendCaseResponseType.FurtherInformation, AmendCaseResponseType.SupportingDocuments)
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(Some(fileUploadedState), Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.FurtherInformationController.onPageLoad(NormalMode).url)

      application.stop()
    }

    "go to Check your request page when no documents need to provided" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(Some(fileUploadedState), Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.AmendCheckYourAnswersController.onPageLoad().url)

      application.stop()
    }

    "go to upload file page" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(Some(fileUploadedState), Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "yes"))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.AmendCaseSendInformationController.showFileUpload(NormalMode).url)

      application.stop()
    }

    "stay on file uploaded page when validation error" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", ""))

      val result = route(application, request).value

      status(result) mustEqual 400
      contentAsString(result) must include(htmlEscapedMessage("error.uploadAnotherFile.required"))
      application.stop()
    }
  }

  "In CheckMode" should {
    "go to Further Information page when both Documents and Further Information selected" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(CheckMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] =
        Set(AmendCaseResponseType.SupportingDocuments, AmendCaseResponseType.FurtherInformation)

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
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(Some(fileUploadedState), Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.FurtherInformationController.onPageLoad(CheckMode).url)

      application.stop()
    }

    "go to Check your Answers page when only Documents is selected" in {
      lazy val uploadAnotherFile =
        routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(CheckMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
        Future.successful(SessionState(Some(fileUploadedState), Some(userAnswers)))
      )
      when(mockSessionRepository.updateSession(anyObject(), anyObject())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.AmendCheckYourAnswersController.onPageLoad().url)

      application.stop()
    }
  }

  "GET /file-verification/:reference/status" should {
    "return file verification status" in {
      def fileVerificationUrl(reference: String) =
        s"${routes.AmendCaseSendInformationController.checkFileVerificationStatus(reference).url}"
      val fileUploadState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Initiated(1, "11370e18-6e24-453e-b45a-76d3e32ea33d"),
            FileUpload.Accepted(
              4,
              "f029444f-415c-4dec-9cf2-36774ec63ab8",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf"
            ),
            FileUpload.Failed(
              3,
              "4b1e15a4-4152-4328-9448-4924d9aee6e2",
              UpscanNotification.FailureDetails(UpscanNotification.QUARANTINE, "some reason")
            )
          )
        ),
        acknowledged = false
      )

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.SupportingDocuments)
      val userAnswers = UserAnswers(userAnswersId).set(
        AmendCaseResponseTypePage,
        amendCaseResponseType
      ).success.value.copy(fileUploadState = Some(fileUploadState))
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = buildRequest(GET, fileVerificationUrl("11370e18-6e24-453e-b45a-76d3e32ea33d"))
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) mustEqual """{"fileStatus":"NOT_UPLOADED"}"""

        val request2 = buildRequest(GET, fileVerificationUrl("f029444f-415c-4dec-9cf2-36774ec63ab8"))
        val result2  = route(application, request2).value
        status(result2) mustEqual 200
        contentAsString(result2) mustEqual """{"fileStatus":"ACCEPTED"}"""

        val request3 = buildRequest(GET, fileVerificationUrl("4b1e15a4-4152-4328-9448-4924d9aee6e2"))
        val result3  = route(application, request3).value
        status(result3) mustEqual 200
        contentAsString(result3) mustEqual """{"fileStatus":"FAILED"}"""

        val request4 = buildRequest(GET, fileVerificationUrl("f0e317f5-d394-42cc-93f8-e89f4fc0114c"))
        val result4  = route(application, request4).value
        status(result4) mustEqual 404
      }
      application.stop()
    }
  }

  def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  def htmlEscapedPageTitle(key: String): String =
    htmlEscapedMessage(key) + " - " + htmlEscapedMessage("site.serviceName") + " - " + htmlEscapedMessage("site.govuk")

  def htmlEscapedPageTitleWithError(key: String): String =
    htmlEscapedMessage("error.browser.title.prefix") + " " + htmlEscapedPageTitle(key)

}
