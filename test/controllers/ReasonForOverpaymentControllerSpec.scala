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
import forms.ReasonForOverpaymentFormProvider
import models.ClaimReasonType.{CommodityCodeChange, CurrencyChanges}
import models.{ClaimDescription, ClaimReasonType}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.{ClaimReasonTypeMultiplePage, ReasonForOverpaymentPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ReasonForOverpaymentView

import scala.concurrent.Future

class ReasonForOverpaymentControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ReasonForOverpaymentFormProvider()
  val form         = formProvider()

  lazy val reasonForOverpaymentRoute = routes.ReasonForOverpaymentController.onPageLoad().url

  val reasons: Set[ClaimReasonType] = Set(CurrencyChanges, CommodityCodeChange)
  val answers                       = emptyUserAnswers

  "ReasonForOverpayment Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, reasonForOverpaymentRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ReasonForOverpaymentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, answers.isMultipleClaimReason, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET for multiple reason claim" in {

      val multipleClaimAnswers = answers.set(ClaimReasonTypeMultiplePage, reasons).success.value
      val application          = applicationBuilder(userAnswers = Some(multipleClaimAnswers)).build()

      val request = FakeRequest(GET, reasonForOverpaymentRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ReasonForOverpaymentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, multipleClaimAnswers.isMultipleClaimReason, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        answers.set(ReasonForOverpaymentPage, ClaimDescription("answer")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, reasonForOverpaymentRoute)

      val view = application.injector.instanceOf[ReasonForOverpaymentView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ClaimDescription("answer")), userAnswers.isMultipleClaimReason, defaultBackLink)(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .build()

      val request =
        FakeRequest(POST, reasonForOverpaymentRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request =
        FakeRequest(POST, reasonForOverpaymentRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ReasonForOverpaymentView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, answers.isMultipleClaimReason, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, reasonForOverpaymentRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, reasonForOverpaymentRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
