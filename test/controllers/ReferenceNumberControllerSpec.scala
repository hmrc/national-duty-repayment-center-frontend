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
import forms.ReferenceNumberFormProvider
import models.UserAnswers
import navigation.NavigatorBack
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReferenceNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ReferenceNumberView

import scala.concurrent.Future

class ReferenceNumberControllerSpec extends SpecBase with MockitoSugar {

  val backLink = NavigatorBack(Some(routes.CreateOrAmendCaseController.onPageLoad))

  val formProvider = new ReferenceNumberFormProvider()
  val form         = formProvider()

  lazy val referenceNumberRoute = routes.ReferenceNumberController.onPageLoad().url

  "ReferenceNumber Controller" must {

    "return OK and the correct view for a GET (with back link)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, referenceNumberRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET from CYA (with back link)" in {

      val userAnswers =
        UserAnswers(userIdentification).copy(changePage = Some(ReferenceNumberPage)).set(
          ReferenceNumberPage,
          "NDRC000A00AB0ABCABC0AB0"
        ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, referenceNumberRoute)

      val view = application.injector.instanceOf[ReferenceNumberView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill("NDRC000A00AB0ABCABC0AB0"), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, referenceNumberRoute)
          .withFormUrlEncodedBody(("value", "NDRC000A00AB0ABCABC0AB0"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, referenceNumberRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ReferenceNumberView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }
  }
}
