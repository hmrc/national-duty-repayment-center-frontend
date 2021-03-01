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
import models.requests.UploadRequest
import models.{CustomsRegulationType, FileUpload, FileUploads, UpscanNotification, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CustomsRegulationTypePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.FileUploaded
import uk.gov.hmrc.http.HeaderCarrier
import java.time.ZonedDateTime

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class BulkFileUploadControllerSpec extends SpecBase with MockitoSugar {
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

  def appBuilder(userAnswers: Option[UserAnswers]): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "metrics.jvm" -> false
      ).overrides(
      bind[DataRequiredAction].to[DataRequiredActionImpl],
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[DataRequiredAction].to[DataRequiredActionImpl],
      bind[UpscanInitiateConnector].toInstance(upscanMock),
      bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
      bind[Metrics].to[MetricsImpl]
    )
  }
  when(upscanMock.initiate(any[UpscanInitiateRequest])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(Future.successful(uscanResponse))

  "GET /file-upload" should {
    "show the upload first document page" in {
      val fileUploadUrl = routes.BulkFileUploadController.showFileUpload().url
      val application =
        appBuilder(userAnswers = Some(emptyUserAnswers))
          .build()
      running(application) {
        val request = buildRequest(GET, fileUploadUrl)
        val result = route(application, request).value
        status(result) mustEqual 200
        contentAsString(result) must include(htmlEscapedMessage("view.upload-file.heading"))
      }
      application.stop()
    }
  }

  "GET /upload-multiple-entries" should {
    "go to entry details page when the CustomsRegulationType is set to UnionsCustomsCodeRegulation" in {
      lazy val uploadFile = routes.BulkFileUploadController.showFileUpload.url

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

      val userAnswers = UserAnswers(userAnswersId).set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation).
        success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = appBuilder(userAnswers = Some(userAnswers.copy(fileUploadState = Some(fileUploadedState)))).build()
      running(application) {
        val request = FakeRequest(GET, uploadFile)
        val result = route(application, request).value
        status(result) mustEqual 200
      }
      application.stop()
    }
    "go to entry details page when the CustomsRegulationType is set to UKCustomsCodeRegulation " in {
      lazy val uploadFile = routes.BulkFileUploadController.showFileUpload.url

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

      val userAnswers = UserAnswers(userAnswersId).set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).
        success.value.copy(fileUploadState = Some(fileUploadedState))

      val application = appBuilder(userAnswers = Some(userAnswers.copy(fileUploadState = Some(fileUploadedState)))).build()
      running(application) {
        val request = FakeRequest(GET, uploadFile)
        val result = route(application, request).value
        status(result) mustEqual 200
      }
      application.stop()
    }

  }

  "GET /file-verification/:reference/status" should {
    "return file verification status" in {
      def fileVerificationUrl(reference: String) = s"${routes.FileUploadController.checkFileVerificationStatus(reference).url}"

      val fileUploadState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Initiated(1, "11370e18-6e24-453e-b45a-76d3e32ea33d"),
            FileUpload.Posted(2, "2b72fe99-8adf-4edb-865e-622ae710f77c"),
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
      val userAnswers = UserAnswers(userAnswersId).set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation).success.value.copy(fileUploadState = Some(fileUploadState))
      val application = appBuilder(userAnswers = Some(userAnswers.copy(fileUploadState = Some(fileUploadState)))).build()

      running(application) {
        val sessionRepo = application.injector.instanceOf[SessionRepository]
        sessionRepo.set(userAnswers)

        val request = buildRequest(GET, fileVerificationUrl("11370e18-6e24-453e-b45a-76d3e32ea33d"))
        val result = route(application, request).value
        status(result) mustEqual 200
        val answers = Await.result(sessionRepo.get(userAnswersId), 10 seconds).value
        contentAsString(result) mustEqual """{"fileStatus":"NOT_UPLOADED"}"""
        answers.fileUploadState.get mustBe fileUploadState


        val request2 = buildRequest(GET, fileVerificationUrl("2b72fe99-8adf-4edb-865e-622ae710f77c"))
        val result2 = route(application, request2).value
        status(result2) mustEqual 200
        val answers2 = Await.result(sessionRepo.get(userAnswersId), 10 seconds).value
        contentAsString(result2) mustEqual """{"fileStatus":"WAITING"}"""
        answers2.fileUploadState.get mustBe fileUploadState


        val request3 = buildRequest(GET, fileVerificationUrl("f029444f-415c-4dec-9cf2-36774ec63ab8"))
        val result3 = route(application, request3).value
        status(result3) mustEqual 200
        val answers3 = Await.result(sessionRepo.get(userAnswersId), 10 seconds).value
        contentAsString(result3) mustEqual """{"fileStatus":"ACCEPTED"}"""
        answers3.fileUploadState.get mustBe fileUploadState

        val request4 = buildRequest(GET, fileVerificationUrl("4b1e15a4-4152-4328-9448-4924d9aee6e2"))
        val result4 = route(application, request4).value
        status(result4) mustEqual 200
        val answers4 = Await.result(sessionRepo.get(userAnswersId), 10 seconds).value
        contentAsString(result4) mustEqual """{"fileStatus":"FAILED"}"""
        answers4.fileUploadState.get mustBe fileUploadState

        val request5 = buildRequest(GET, fileVerificationUrl("f0e317f5-d394-42cc-93f8-e89f4fc0114c"))
        val result5 = route(application, request5).value
        status(result5) mustEqual 404
        val answers5 = Await.result(sessionRepo.get(userAnswersId), 10 seconds).value
        answers5.fileUploadState.get mustBe fileUploadState
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