/*
 * Copyright 2025 HM Revenue & Customs
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
import models.FileType.Bulk
import models.requests.UploadRequest
import models.{FileUpload, FileUploads, SessionState, UpscanNotification, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import play.api.http.Status.SEE_OTHER
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.{
  contentAsString,
  defaultAwaitTimeout,
  redirectLocation,
  route,
  running,
  status,
  writeableOf_AnyContentAsEmpty,
  writeableOf_AnyContentAsFormUrlEncoded,
  GET,
  POST
}
import play.twirl.api.HtmlFormat
import services.{FileUploadState, FileUploaded, UploadFile}

import java.time.ZonedDateTime
import scala.concurrent.Future

class BulkFileUploadControllerSpec extends SpecBase with MockitoSugar {
  val id = "1"

  "GET /file-upload" should {
    "show the upload first document page" in {
      val fileUploadUrl = routes.BulkFileUploadController.showFileUpload().url
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()
      running(application) {
        when(mockSessionRepository.getFileUploadState(userAnswersId)).thenReturn(
          Future.successful(SessionState(None, Some(emptyUserAnswers)))
        )
        when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)
        val request = FakeRequest(GET, fileUploadUrl)
        val result  = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include(htmlEscapedMessage("bulkFileUpload.heading"))
      }
      application.stop()
    }
  }

  "GET /upload-multiple-entries" should {
    "go to entry details page when the CustomsRegulationType is set to UnionsCustomsCodeRegulation" in {
      lazy val uploadFile = routes.BulkFileUploadController.showFileUpload().url

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

      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadedState))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = FakeRequest(GET, uploadFile)
        val result  = route(application, request).value
        status(result) mustEqual 200
      }
      application.stop()
    }
    "go to entry details page when the CustomsRegulationType is set to UKCustomsCodeRegulation " in {
      lazy val uploadFile = routes.BulkFileUploadController.showFileUpload().url

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

      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadedState))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = FakeRequest(GET, uploadFile)
        val result  = route(application, request).value
        status(result) mustEqual 200
      }
      application.stop()
    }

  }

  "GET /file-verification/:reference/status" should {
    "return file verification status" in {
      def fileVerificationUrl(reference: String) =
        s"${routes.BulkFileUploadController.checkFileVerificationStatus(reference).url}"

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
      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadState))
      val application =
        applicationBuilder(userAnswers = Some(userAnswers.copy(fileUploadState = Some(fileUploadState)))).build()

      running(application) {
        when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

        val request = FakeRequest(GET, fileVerificationUrl("11370e18-6e24-453e-b45a-76d3e32ea33d"))
        val result1 = route(application, request).value
        status(result1) mustEqual 200
        contentAsString(result1) mustEqual """{"fileStatus":"NOT_UPLOADED"}"""

        val request2 = FakeRequest(GET, fileVerificationUrl("f029444f-415c-4dec-9cf2-36774ec63ab8"))
        val result2  = route(application, request2).value
        status(result2) mustEqual 200
        contentAsString(result2) mustEqual """{"fileStatus":"ACCEPTED"}"""

        val request3 = FakeRequest(GET, fileVerificationUrl("4b1e15a4-4152-4328-9448-4924d9aee6e2"))
        val result3  = route(application, request3).value
        status(result3) mustEqual 200
        contentAsString(result3) mustEqual """{"fileStatus":"FAILED"}"""

        val request4 = FakeRequest(GET, fileVerificationUrl("f0e317f5-d394-42cc-93f8-e89f4fc0114c"))
        val result4  = route(application, request4).value
        status(result4) mustEqual 404
      }
      application.stop()
    }
  }

  "GET /file-verification" should {
    "redirect to /upload-multiple-entries when file is uploaded" in {

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
        ),
        acknowledged = false
      )
      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadState), Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.BulkFileUploadController.showWaitingForFileVerification().url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustBe routes.BulkFileUploadController.showFileUpload().url

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

        val request = FakeRequest(GET, routes.BulkFileUploadController.showWaitingForFileVerification().url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustBe routes.BulkFileUploadController.showFileUpload().url

      }
      application.stop()
    }
  }

  "POST /upload-multiple-entries/continue " should {
    "redirect to next page when bulk file has already been uploaded" in {

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
              Some(Bulk)
            )
          )
        )
      )

      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadState))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.BulkFileUploadController.onContinue().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual defaultNextPage.url
      }
      application.stop()

    }

    "display error when bulk file not uploaded" in {

      val fileUploadState = FileUploaded(
        FileUploads(files =
          Seq(FileUpload.Initiated(1, "11370e18-6e24-453e-b45a-76d3e32ea33d"))
        )
      )

      val userAnswers = UserAnswers(userIdentification).copy(fileUploadState = Some(fileUploadState))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.BulkFileUploadController.onContinue().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include(routes.BulkFileUploadController.markFileUploadAsRejected().url)
        redirectLocation(result).value must include("errorCode=MissingFile")
      }
      application.stop()

    }
  }

  "GET /upload-multiple-entries/remove " should {
    "remove existing bulk upload" in {

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
              Some(Bulk)
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
        val request = FakeRequest(GET, routes.BulkFileUploadController.onRemove("foo-bar-ref-1").url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BulkFileUploadController.showFileUpload().url
      }
      application.stop()

      updatedState.getValue.fileUploads.isEmpty mustBe true
    }

    "silently redirect to /upload-multiple-entries when file upload state is missing" in {
      val userAnswers = emptyUserAnswers

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.BulkFileUploadController.onRemove("foo-bar-ref-1").url)
        val result  = route(application, request).value

        status(result) mustEqual 303
        redirectLocation(result).value mustEqual routes.BulkFileUploadController.showFileUpload().url
      }
      application.stop()
    }
  }

  "GET /file-rejected" should {
    "return BAD_REQUEST and render page with error" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.BulkFileUploadController.markFileUploadAsRejected().url
        ).withFormUrlEncodedBody("key" -> "")
        val result = route(application, request).value

        status(result) mustEqual 400
      }
      application.stop()
    }

    "redirect to showFileUpload page when errorCode is MissingFile" in {

      val fileUploadState = UploadFile(
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
      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadState), Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.BulkFileUploadController.markFileUploadAsRejected().url
        ).withFormUrlEncodedBody("key" -> "key", "errorCode" -> "MissingFile", "errorMessage" -> "errorMessage")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BulkFileUploadController.showFileUpload().url
      }
      application.stop()
    }

    "redirect to showFileUpload page when fileUploadState if None" in {

      val userAnswers = emptyUserAnswers.copy(fileUploadState = None)

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.BulkFileUploadController.markFileUploadAsRejected().url
        ).withFormUrlEncodedBody("key" -> "key", "errorCode" -> "MissingFile", "errorMessage" -> "errorMessage")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BulkFileUploadController.showFileUpload().url
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
