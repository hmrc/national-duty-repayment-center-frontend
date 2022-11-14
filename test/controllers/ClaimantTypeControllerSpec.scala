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
import forms.ClaimantTypeFormProvider
import models.{ClaimantType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.ClaimantTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ClaimantTypeView

import scala.concurrent.Future

class ClaimantTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val claimantTypeRoute = routes.ClaimantTypeController.onPageLoad().url

  val formProvider = new ClaimantTypeFormProvider()
  val form         = formProvider()

  "ClaimantType Controller" must {

    "return OK and the correct view for a GET (with back link)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, claimantTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimantTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET from CYA (with back link)" in {

      val userAnswers = UserAnswers(userIdentification).copy(changePage = Some(ClaimantTypePage)).set(
        ClaimantTypePage,
        ClaimantType.values.head
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, claimantTypeRoute)

      val view = application.injector.instanceOf[ClaimantTypeView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ClaimantType.values.head), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val userAnswers = UserAnswers(userIdentification).set(ClaimantTypePage, ClaimantType.values.head).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      val request =
        FakeRequest(POST, claimantTypeRoute)
          .withFormUrlEncodedBody(("value", ClaimantType.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, claimantTypeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[ClaimantTypeView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }
  }
}
