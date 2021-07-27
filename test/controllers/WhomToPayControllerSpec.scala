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
import forms.WhomToPayFormProvider
import models.{BankDetails, UserAnswers, WhomToPay}
import navigation.CreateNavigator
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{atLeastOnce, times, verify, when}
import org.mockito.internal.verification.Times
import org.scalatestplus.mockito.MockitoSugar
import pages.{BankDetailsPage, IndirectRepresentativePage, WhomToPayPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhomToPayView

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class WhomToPayControllerSpec extends SpecBase with MockitoSugar {

  lazy val whomToPayRoute = routes.WhomToPayController.onPageLoad().url

  val formProvider = new WhomToPayFormProvider()
  val form         = formProvider()

  "WhomToPay Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, whomToPayRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhomToPayView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "removes bank details if answer changes" in {

      val userAnswers = UserAnswers(userIdentification).set(WhomToPayPage, WhomToPay.Representative).success.value

      val updatedAnswers =
        userAnswers.set(BankDetailsPage, BankDetails("Natural Numbers Inc", "123456", "12345678")).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers), mockCreateNavigator)
          .build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      val answerCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockCreateNavigator).nextPage(any(), answerCaptor.capture())

      answerCaptor.getValue.get(BankDetailsPage) mustBe None

      application.stop()
    }

    " does not remove bank details if answer does not change" in {

      val userAnswers = UserAnswers(userIdentification).set(WhomToPayPage, WhomToPay.Importer).success.value

      val updatedAnswers =
        userAnswers.set(BankDetailsPage, BankDetails("Natural Numbers Inc", "123456", "12345678")).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers), mockCreateNavigator)
          .build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      val answerCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockCreateNavigator, atLeastOnce()).nextPage(any(), answerCaptor.capture())

      answerCaptor.getValue.get(BankDetailsPage) mustBe Some(BankDetails("Natural Numbers Inc", "123456", "12345678"))

      application.stop()
    }

    "removes indirect representative if answer changes" in {

      val userAnswers = UserAnswers(userIdentification).set(WhomToPayPage, WhomToPay.Representative).success.value

      val updatedAnswers = userAnswers.set(IndirectRepresentativePage, true).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers))
          .build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      val answerCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockCreateNavigator, atLeastOnce()).nextPage(any(), answerCaptor.capture())

      answerCaptor.getValue.get(IndirectRepresentativePage) mustBe None

      application.stop()
    }

    "does not remove indirect representative if answer does not change" in {

      val userAnswers = UserAnswers(userIdentification).set(WhomToPayPage, WhomToPay.Importer).success.value

      val updatedAnswers = userAnswers.set(IndirectRepresentativePage, true).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers))
          .build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      val answerCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockCreateNavigator, atLeastOnce()).nextPage(any(), answerCaptor.capture())

      answerCaptor.getValue.get(IndirectRepresentativePage) mustBe Some(true)

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userIdentification).set(WhomToPayPage, WhomToPay.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, whomToPayRoute)

      val view = application.injector.instanceOf[WhomToPayView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(WhomToPay.values.head), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhomToPayView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, whomToPayRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whomToPayRoute)
          .withFormUrlEncodedBody(("value", WhomToPay.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
