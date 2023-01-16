/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ClaimantType.{Importer, Representative}
import models.FileType.SupportingEvidence
import models._
import models.requests.UploadRequest
import navigation.CreateNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import pages.{AgentImporterHasEORIPage, ClaimantTypePage, ImporterHasEoriPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.{FileUploadState, FileUploaded, UploadFile}

import java.time.ZonedDateTime
import scala.concurrent.Future

class FileUploadControllerSpec extends SpecBase with MockitoSugar {
  val id = "1"

  val currentState: UploadFile =
    UploadFile(
      "foo-bar-ref-2",
      UploadRequest(
        href = "https://s3.bucket",
        fields = Map(
          "callbackUrl"     -> "https://foo.bar/callback",
          "successRedirect" -> "https://foo.bar/success",
          "errorRedirect"   -> "https://foo.bar/failure"
        )
      ),
      FileUploads(files =
        Seq(FileUpload.Initiated(1, "foo-bar-ref-1"))
      )
    )

  "GET /file-upload" should {
    "show the upload first document page" in {
      val fileUploadUrl = routes.FileUploadController.showFileUpload().url
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()
      running(application) {
        when(mockSessionRepository.getFileUploadState(emptyUserAnswers.id)) thenReturn Future.successful(
          SessionState(Some(currentState), Some(emptyUserAnswers))
        )
        when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)

        val request = buildRequest(GET, fileUploadUrl)
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include(htmlEscapedMessage("view.upload-file.heading"))
      }
      application.stop()
    }

    "show file uploaded page" in {
      val fileUploadedUrl = routes.FileUploadController.showFileUpload().url

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "uploaded-file.pdf",
              "application/pdf",
              Some(SupportingEvidence)
            )
          )
        ),
        acknowledged = true
      )
      val userAnswers = UserAnswers(userIdentification).set(
        AgentImporterHasEORIPage,
        AgentImporterHasEORI.values.head
      ).success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        when(mockSessionRepository.getFileUploadState(userAnswersId)) thenReturn Future.successful(
          SessionState(Some(fileUploadedState), Some(userAnswers))
        )
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)
        val request = buildRequest(GET, fileUploadedUrl)
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include("uploaded-file.pdf")
      }
      application.stop()
    }
  }

  "POST /file-upload" should {
    "go to Agent has EORI page when file uploaded" in {
      lazy val uploadAnotherFile = routes.FileUploadController.onContinue().url

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
      val userAnswers =
        UserAnswers(userIdentification).set(ClaimantTypePage, Representative).success.value.copy(fileUploadState =
          Some(fileUploadedState)
        )
      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        createNavigator = injector.instanceOf[CreateNavigator]
      ).build()
      when(mockSessionRepository.getFileUploadState(userAnswersId)) thenReturn Future.successful(
        SessionState(Some(fileUploadedState), Some(userAnswers))
      )
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
      val result  = route(application, request).value

      redirectLocation(result) mustBe Some(routes.AgentImporterHasEORIController.onPageLoad().url)

      application.stop()
    }

    "go to Importer has EORI page when file uploaded" in {
      lazy val uploadAnotherFile = routes.FileUploadController.onContinue().url

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

      val userAnswers = UserAnswers(userIdentification).set(ImporterHasEoriPage, true).success.value.set(
        ClaimantTypePage,
        Importer
      ).success.value.copy(fileUploadState = Some(fileUploadedState))
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), createNavigator = injector.instanceOf[CreateNavigator])
          .build()
      when(mockSessionRepository.getFileUploadState(userAnswersId)) thenReturn Future.successful(
        SessionState(Some(fileUploadedState), Some(userAnswers))
      )
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val request = FakeRequest(POST, uploadAnotherFile)
      val result  = route(application, request).value

      redirectLocation(result) mustBe Some(routes.ImporterHasEoriController.onPageLoad().url)

      application.stop()
    }

    "error if no file uploaded" in {
      lazy val continueUrl = routes.FileUploadController.onContinue().url

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(FileUpload.Initiated(1, "11370e18-6e24-453e-b45a-76d3e32ea33d"))
        ),
        acknowledged = true
      )

      val userAnswers =
        UserAnswers(userIdentification).set(ClaimantTypePage, Importer).success.value.copy(fileUploadState =
          Some(fileUploadedState)
        )
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), createNavigator = injector.instanceOf[CreateNavigator])
          .build()
      when(mockSessionRepository.getFileUploadState(userAnswersId)) thenReturn Future.successful(
        SessionState(Some(fileUploadedState), Some(userAnswers))
      )
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val request = FakeRequest(POST, continueUrl)
      val result  = route(application, request).value

      status(result) mustEqual 303
      redirectLocation(result).value must include(routes.FileUploadController.markFileUploadAsRejected().url)
      redirectLocation(result).value must include("errorCode=MissingFile")

      application.stop()
    }
  }

  "GET /file-verification/:reference/status" should {
    "return file verification status" in {
      def fileVerificationUrl(reference: String) =
        s"${routes.FileUploadController.checkFileVerificationStatus(reference).url}"

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
        )
      )

      val userAnswers = UserAnswers(userIdentification).set(
        AgentImporterHasEORIPage,
        AgentImporterHasEORI.values.head
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

  "GET /file-verification" should {
    "redirect to /file-upload when file is uploaded" in {

      val fileUploadState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "f029444f-415c-4dec-9cf2-36774ec63ab8",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf"
            )
          )
        )
      )
      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadState), Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request = buildRequest(GET, routes.FileUploadController.showWaitingForFileVerification().url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustBe routes.FileUploadController.showFileUpload().url

      }
      application.stop()
    }

    "silently redirect to /file-upload when file upload state is missing" in {
      val userAnswers = emptyUserAnswers

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request = buildRequest(GET, routes.FileUploadController.showWaitingForFileVerification().url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustBe routes.FileUploadController.showFileUpload().url

      }
      application.stop()
    }
  }

  "GET /file-uploaded/:reference/remove  " should {
    "remove existing upload" in {

      val fileUploadState = FileUploaded(
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
        )
      )

      val updatedState: ArgumentCaptor[FileUploadState] = ArgumentCaptor.forClass(classOf[FileUploadState])
      when(mockSessionRepository.updateSession(updatedState.capture(), any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadState))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = buildRequest(GET, routes.FileUploadController.onRemove("foo-bar-ref-1").url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustEqual routes.FileUploadController.showFileUpload().url
      }
      application.stop()

      updatedState.getValue.fileUploads.isEmpty mustBe true
    }

    "silently redirect to /file-upload when file upload state is missing" in {
      val userAnswers = emptyUserAnswers

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = buildRequest(GET, routes.FileUploadController.onRemove("foo-bar-ref-1").url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustEqual routes.FileUploadController.showFileUpload().url
      }
      application.stop()
    }

  }

  "showFileUpload" should {

    "return 200" when {

      "the file upload has been successful" in {

        when(mockSessionRepository.getFileUploadState(any())).thenReturn(
          Future.successful(SessionState(Some(currentState), Some(emptyUserAnswers)))
        )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = buildRequest(GET, routes.FileUploadController.showFileUpload().url)
          val result  = route(application, request).value

          status(result) mustEqual 200
        }

        application.stop()
      }

      "file size is too large and returns the correct error in the response" in {

        val fileUploadError: Option[FileTransmissionFailed] = Some(
          FileTransmissionFailed(
            S3UploadError(
              id,
              "EntityTooLarge",
              "Your proposed upload exceeds the maximum allowed size",
              Some("SomeRequestId"),
              Some("NoFileReference")
            )
          )
        )

        val uploadFileStateWithError = currentState.copy(maybeUploadError = fileUploadError)

        val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(uploadFileStateWithError))

        when(mockSessionRepository.getFileUploadState(any())).thenReturn(
          Future.successful(SessionState(Some(uploadFileStateWithError), Some(userAnswers)))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = buildRequest(GET, routes.FileUploadController.showFileUpload().url)
          val result  = route(application, request).value

          status(result) mustEqual 200
          contentAsString(result) must include(messages("error.file-upload.invalid-size-large"))
        }
        application.stop()
      }
    }
  }

  def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  def htmlEscapedPageTitle(key: String): String =
    htmlEscapedMessage(key) + " - " + htmlEscapedMessage("site.serviceName") + " - " + htmlEscapedMessage("site.govuk")

  def htmlEscapedPageTitleWithError(key: String): String =
    htmlEscapedMessage("error.browser.title.prefix") + " " + htmlEscapedPageTitle(key)

}
