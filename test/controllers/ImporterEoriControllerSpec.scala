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
import forms.ImporterEoriFormProvider
import models.ClaimantType.Importer
import models.{EORI, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.{ClaimantTypePage, ImporterEoriPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ImporterEoriView

import scala.concurrent.Future

class ImporterEoriControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ImporterEoriFormProvider()
  val form         = formProvider()

  lazy val importerEoriRoute = routes.ImporterEoriController.onPageLoad().url

  "ImporterEori Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, importerEoriRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ImporterEoriView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET importers journey" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(ClaimantTypePage, Importer).success.value)).build()

      val request = FakeRequest(GET, importerEoriRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ImporterEoriView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userIdentification).set(ImporterEoriPage, EORI("answer")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, importerEoriRoute)

      val view = application.injector.instanceOf[ImporterEoriView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(EORI("answer")), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, importerEoriRoute)
          .withFormUrlEncodedBody(("value", "GB123456123456"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "redirect to the next page when valid data is submitted Importers journey" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(ClaimantTypePage, Importer).success.value))
          .build()

      val request =
        FakeRequest(POST, importerEoriRoute)
          .withFormUrlEncodedBody(("value", "GB123456123456"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, importerEoriRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ImporterEoriView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, importerEoriRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, importerEoriRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
