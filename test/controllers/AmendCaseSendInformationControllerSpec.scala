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
import models.{AmendCaseResponseType, FileUpload, FileUploads, NormalMode, UpscanNotification, UserAnswers}
import org.mockito.Matchers.{any, anyObject}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AmendCaseResponseTypePage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.FileUploaded
import uk.gov.hmrc.http.HeaderCarrier

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}

class AmendCaseSendInformationControllerSpec extends SpecBase with MockitoSugar {
  val id = "1"
  val uscanResponse =
    UpscanInitiateResponse(
      reference = "foo-bar-ref",
      uploadRequest =
        UploadRequest(href = "https://s3.bucket", fields = Map("callbackUrl" -> "https://foo.bar/callback"))
    )

  def buildRequest(method: String, path: String): FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest(method, path)
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  }
  val upscanMock = mock[UpscanInitiateConnector]
  val mockSessionRepository = mock[SessionRepository]

  def appBuilder(userAnswers: Option[UserAnswers]): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "metrics.jvm" -> false
      ).overrides(
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[SessionRepository].toInstance(mockSessionRepository),
      bind[UpscanInitiateConnector].toInstance(upscanMock),
      bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
      bind[Metrics].to[MetricsImpl]
    )
  }
  when(upscanMock.initiate(any[UpscanInitiateRequest])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(Future.successful(uscanResponse))

  "GET /file-upload" should {
    "show the upload first document page" in {
      val fileUploadUrl = routes.AmendCaseSendInformationController.showFileUpload().url
      val application = appBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(anyObject())) thenReturn Future.successful(true)
        val request = buildRequest(GET, fileUploadUrl)
        val result = route(application, request).value
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
      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Furtherinformation, AmendCaseResponseType.Supportingdocuments)
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadedState))

      val mockSessionRepository = mock[SessionRepository]
      val application = appBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = buildRequest(GET, fileUploadedUrl)
        val result = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include("You have uploaded 1 file")
      }
      application.stop()
    }
  }

  "POST /file-uploaded" should {
    "go to Further information page" in {
      lazy val uploadAnotherFile = routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

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
      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Furtherinformation, AmendCaseResponseType.Supportingdocuments)
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = appBuilder(userAnswers = Some(userAnswers)).build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.FurtherInformationController.onPageLoad(NormalMode).url)

      application.stop()
    }

    "go to Check your request page when no documents need to provided" in {
      lazy val uploadAnotherFile = routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Supportingdocuments)


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
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadedState))


      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application = appBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "no"))

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.AmendCheckYourAnswersController.onPageLoad().url)

      application.stop()
    }

    "go to upload file page" in {
      lazy val uploadAnotherFile = routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Supportingdocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(anyObject())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", "yes"))

      val application = appBuilder(userAnswers = Some(userAnswers)).build()

      val result = route(application, request).value

      redirectLocation(result) mustEqual Some(routes.AmendCaseSendInformationController.showFileUpload().url)

      application.stop()
    }

    "stay on file uploaded page when validation error" in {
      lazy val uploadAnotherFile = routes.AmendCaseSendInformationController.submitUploadAnotherFileChoice(NormalMode).url

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Supportingdocuments)

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
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadedState))

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application = appBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, uploadAnotherFile)
        .withFormUrlEncodedBody(("uploadAnotherFile", ""))

      val result = route(application, request).value

      status(result) mustEqual 400
      contentAsString(result) must include(htmlEscapedMessage("error.uploadAnotherFile.required"))
      application.stop()
    }
  }

  "GET /file-verification/:reference/status" should {
    "return file verification status" in {
      def fileVerificationUrl(reference: String) = s"${routes.AmendCaseSendInformationController.checkFileVerificationStatus(reference).url}"
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

      val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Supportingdocuments)
      val userAnswers = UserAnswers(userAnswersId).set(AmendCaseResponseTypePage, amendCaseResponseType).success.value.copy(fileUploadState = Some(fileUploadState))
      val application = appBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = buildRequest(GET, fileVerificationUrl("11370e18-6e24-453e-b45a-76d3e32ea33d"))
        val result = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) mustEqual """{"fileStatus":"NOT_UPLOADED"}"""

        val request2 = buildRequest(GET, fileVerificationUrl("f029444f-415c-4dec-9cf2-36774ec63ab8"))
        val result2 = route(application, request2).value
        status(result2) mustEqual 200
        contentAsString(result2) mustEqual """{"fileStatus":"ACCEPTED"}"""

        val request3 = buildRequest(GET, fileVerificationUrl("4b1e15a4-4152-4328-9448-4924d9aee6e2"))
        val result3 = route(application, request3).value
        status(result3) mustEqual 200
        contentAsString(result3) mustEqual """{"fileStatus":"FAILED"}"""

        val request4 = buildRequest(GET, fileVerificationUrl("f0e317f5-d394-42cc-93f8-e89f4fc0114c"))
        val result4 = route(application, request4).value
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
