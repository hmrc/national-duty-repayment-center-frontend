/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.DeclarantReferenceNumberFormProvider
import models.{DeclarantReferenceNumber, DeclarantReferenceType, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.DeclarantReferenceNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DeclarantReferenceNumberView

import scala.concurrent.Future

class DeclarantReferenceNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new DeclarantReferenceNumberFormProvider()
  val form         = formProvider()

  lazy val declarantReferenceNumberRoute = routes.DeclarantReferenceNumberController.onPageLoad().url

  "DeclarantReferenceNumber Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, declarantReferenceNumberRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DeclarantReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userIdentification).set(
        DeclarantReferenceNumberPage,
        DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some("123"))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, declarantReferenceNumberRoute)

      val view = application.injector.instanceOf[DeclarantReferenceNumberView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some("123"))), defaultBackLink)(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, declarantReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", "01"), ("declarantReferenceNumber", "01"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, declarantReferenceNumberRoute)
          .withFormUrlEncodedBody(("test", ""))

      val boundForm = form.bind(Map("test" -> ""))

      val view = application.injector.instanceOf[DeclarantReferenceNumberView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, declarantReferenceNumberRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, declarantReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", "123"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
