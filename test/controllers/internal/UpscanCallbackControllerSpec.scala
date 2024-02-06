/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.internal

import base.SpecBase
import models.FileType.SupportingEvidence
import models._
import models.requests.UploadRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FileUploaded, UploadFile}

import java.time.ZonedDateTime
import scala.concurrent.Future

class UpscanCallbackControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    super.beforeEach()
  }

  val jsonRequest: JsValue = Json.parse(
    """{
      |"reference":"11370e18-6e24-453e-b45a-76d3e32ea33d",
      |"fileStatus":"READY",
      |"downloadUrl":"https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      |"uploadDetails":{
      |"uploadTimestamp":"2018-04-24T09:30:00Z",
      |"checksum":"396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
      |"fileName":"test.pdf",
      |"fileMimeType":"application/pdf"
      |}
      |}""".stripMargin
  )

  val fileUploadRejectedState: FileUploaded = FileUploaded(
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
        ),
        FileUpload.TimedOut(5, "8d3eae37-fdeb-411c-b38c-0380be472fbc", Some(FileType.SupportingEvidence))
      )
    ),
    acknowledged = false
  )

  val fileUploadAcceptedState: UploadFile = UploadFile(
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
      Seq(
        FileUpload.Accepted(
          1,
          "f029444f-415c-4dec-9cf2-36774ec63ab8",
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

  "POST /callback-from-upscan/upload-proof-of-authority/:id" should {

    "return NO_CONTENT when fileUploadState is of type UploadFile" in {

      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadAcceptedState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadAcceptedState), Some(userAnswers)))
      )

      when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, routes.UpscanCallbackController.proofOfAuthorityCallbackFromUpscan("id").url).withJsonBody(
            jsonRequest
          )
        val result = route(application, request).value
        status(result) mustBe NO_CONTENT
      }
      application.stop()
    }

    "return CREATED when fileUploadState is of type FileUploaded" in {

      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadRejectedState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadRejectedState), Some(userAnswers)))
      )

      when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, routes.UpscanCallbackController.proofOfAuthorityCallbackFromUpscan("id").url).withJsonBody(
            jsonRequest
          )
        val result = route(application, request).value
        status(result) mustBe CREATED
      }
      application.stop()
    }

    "return fileStateError when fileUploadState is `None`" in {
      val userAnswers = emptyUserAnswers

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(None, Some(userAnswers)))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, routes.UpscanCallbackController.proofOfAuthorityCallbackFromUpscan("id").url).withJsonBody(
            jsonRequest
          )
        val result = intercept[Exception](route(application, request).value.futureValue)
        result.toString must include("File upload state error")

      }
      application.stop()
    }

  }

  "POST /callback-from-upscan/bulk/:id" should {

    "return NO_CONTENT when fileUploadState is of type UploadFile" in {

      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadAcceptedState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadAcceptedState), Some(userAnswers)))
      )

      when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, routes.UpscanCallbackController.bulkUploadCallbackFromUpscan("id").url).withJsonBody(
            jsonRequest
          )
        val result = route(application, request).value
        status(result) mustBe NO_CONTENT
      }
      application.stop()
    }

  }

  "POST /callback-from-upscan" should {

    "return NO_CONTENT when fileUploadState is of type UploadFile" in {

      val userAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadAcceptedState))

      when(mockSessionRepository.getFileUploadState(any())).thenReturn(
        Future.successful(SessionState(Some(fileUploadAcceptedState), Some(userAnswers)))
      )

      when(mockSessionRepository.updateSession(any(), any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {

        val request =
          FakeRequest(POST, routes.UpscanCallbackController.bulkUploadCallbackFromUpscan("id").url).withJsonBody(
            jsonRequest
          )
        val result = route(application, request).value
        status(result) mustBe NO_CONTENT
      }
      application.stop()
    }
  }
}
