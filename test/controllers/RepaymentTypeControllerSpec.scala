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
import forms.RepaymentTypeFormProvider
import models.RepaymentType.CMA
import models.{BankDetails, RepaymentType, UserAnswers, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.RepaymentTypeView

import java.time.LocalDate
import scala.concurrent.Future

class RepaymentTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val repaymentTypeRoute = routes.RepaymentTypeController.onPageLoad().url

  val formProvider = new RepaymentTypeFormProvider()
  val form         = formProvider()

  val duties: Set[ClaimRepaymentType] = Set(ClaimRepaymentType.Customs)

  val validAnswersForRepaymentType = emptyUserAnswers
    .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value
    .set(ClaimRepaymentTypePage, duties).success.value
    .set(CustomsDutyPaidPage, RepaymentAmounts("250", "0")).success.value
    .set(EntryDetailsPage, EntryDetails("123", "123456Q", LocalDate.now().minusDays(1))).success.value

  "RepaymentType Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(validAnswersForRepaymentType)).build()

      val request = FakeRequest(GET, repaymentTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RepaymentTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect if RepaymentType not valid on a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, repaymentTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = validAnswersForRepaymentType.set(RepaymentTypePage, RepaymentType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, repaymentTypeRoute)

      val view = application.injector.instanceOf[RepaymentTypeView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(RepaymentType.values.head), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(validAnswersForRepaymentType))
          .build()

      val request =
        FakeRequest(POST, repaymentTypeRoute)
          .withFormUrlEncodedBody(("value", RepaymentType.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "update user answers when valid data is submitted " in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val userAnswers = validAnswersForRepaymentType.set(ClaimantTypePage, ClaimantType.Importer).flatMap(
        _.set(BankDetailsPage, BankDetails("name", "123456", "12345678"))
      ).get

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      val request =
        FakeRequest(POST, repaymentTypeRoute)
          .withFormUrlEncodedBody(("value", CMA.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      persistedAnswers.getValue.get(RepaymentTypePage) mustBe Some(CMA)
      persistedAnswers.getValue.get(BankDetailsPage) mustBe None // not required for CMA
      persistedAnswers.getValue.get(WhomToPayPage) mustBe Some(WhomToPay.Importer)

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, repaymentTypeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[RepaymentTypeView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, repaymentTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, repaymentTypeRoute)
          .withFormUrlEncodedBody(("value", RepaymentType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
