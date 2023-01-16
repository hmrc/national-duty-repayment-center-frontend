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
import forms.AmendCaseResponseTypeFormProvider
import models.AmendCaseResponseType.{FurtherInformation, SupportingDocuments}
import models.{AmendCaseResponseType, FileUpload, FileUploads, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import pages.{AmendCaseResponseTypePage, FurtherInformationPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FileUploaded
import views.html.AmendCaseResponseTypeView

import java.time.ZonedDateTime
import scala.concurrent.Future

class AmendCaseResponseTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val amendCaseResponseTypeRoute = routes.AmendCaseResponseTypeController.onPageLoad().url

  val formProvider = new AmendCaseResponseTypeFormProvider()
  val form         = formProvider()

  "AmendCaseResponseType Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, amendCaseResponseTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendCaseResponseTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userIdentification).set(AmendCaseResponseTypePage, AmendCaseResponseType.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, amendCaseResponseTypeRoute)

      val view = application.injector.instanceOf[AmendCaseResponseTypeView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(AmendCaseResponseType.values.toSet), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, amendCaseResponseTypeRoute)
          .withFormUrlEncodedBody(("value[0]", AmendCaseResponseType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "remove existing further information when not selected" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val userAnswersWithFurtherInformation = emptyUserAnswers.set(FurtherInformationPage, "further information").get
      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithFurtherInformation)).build()

      val request =
        FakeRequest(POST, amendCaseResponseTypeRoute)
          .withFormUrlEncodedBody(("value[0]", SupportingDocuments.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      persistedAnswers.getValue().get(FurtherInformationPage) mustBe None

      application.stop()
    }

    "remove existing supporting documents when not selected" in {

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

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val userAnswersWithSupportingDocuments = emptyUserAnswers.copy(fileUploadState = Some(fileUploadedState))
      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSupportingDocuments)).build()

      val request =
        FakeRequest(POST, amendCaseResponseTypeRoute)
          .withFormUrlEncodedBody(("value[0]", FurtherInformation.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      persistedAnswers.getValue().fileUploadState mustBe None

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, amendCaseResponseTypeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[AmendCaseResponseTypeView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, amendCaseResponseTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, amendCaseResponseTypeRoute)
          .withFormUrlEncodedBody(("value[0]", AmendCaseResponseType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
