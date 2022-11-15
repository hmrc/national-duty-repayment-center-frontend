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
import forms.{EmailAddressAndPhoneNumberFormProvider, EmailAndPhoneNumber}
import models.IsContactProvided.Email
import models.{IsContactProvided, UserAnswers}
import org.mockito.{ArgumentCaptor, MockitoSugar}
import pages.EmailAddressAndPhoneNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.EmailAddressAndPhoneNumberView

import scala.concurrent.Future

class EmailAddressAndPhoneNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new EmailAddressAndPhoneNumberFormProvider()
  val form         = formProvider()

  lazy val emailAddressRoute = routes.EmailAddressAndPhoneNumberController.onPageLoad().url

  "EmailAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, emailAddressRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[EmailAddressAndPhoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userIdentification).set(
        EmailAddressAndPhoneNumberPage,
        EmailAndPhoneNumber(Set(IsContactProvided.Email), Some("test@testing.com"), Some(""))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, emailAddressRoute)

      val view = application.injector.instanceOf[EmailAddressAndPhoneNumberView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          form.fill(EmailAndPhoneNumber(Set(IsContactProvided.Email), Some("test@testing.com"), Some(""))),
          defaultBackLink
        )(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, emailAddressRoute)
          .withFormUrlEncodedBody(("value[]", "01"), ("email", "a@b.com"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      persistedAnswers.getValue.get(EmailAddressAndPhoneNumberPage) mustBe Some(
        EmailAndPhoneNumber(Set(Email), Some("a@b.com"), None)
      )

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, emailAddressRoute)
          .withFormUrlEncodedBody(("email", ""))

      val boundForm = form.bind(Map("email" -> ""))

      val view = application.injector.instanceOf[EmailAddressAndPhoneNumberView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, emailAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, emailAddressRoute)
          .withFormUrlEncodedBody(("email", "test@test.com"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
