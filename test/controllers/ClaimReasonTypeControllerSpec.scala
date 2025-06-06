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
import forms.ClaimReasonTypeFormProvider
import models.ClaimReasonType
import models.ClaimReasonType.{CommodityCodeChange, CurrencyChanges}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar
import pages.{ClaimReasonTypeMultiplePage, ClaimReasonTypePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ClaimReasonTypeView

import scala.concurrent.Future

class ClaimReasonTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val claimReasonTypeRoute = routes.ClaimReasonTypeController.onPageLoad().url

  val formProvider = new ClaimReasonTypeFormProvider()
  val form         = formProvider()

  val reasons: Set[ClaimReasonType] = Set(CurrencyChanges, CommodityCodeChange)

  val multipleClaimReasonsAnswers =
    emptyUserAnswers.set(ClaimReasonTypeMultiplePage, reasons).success.value

  "ClaimReasonType Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers)).build()

      val request = FakeRequest(GET, claimReasonTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimReasonTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, reasons, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        multipleClaimReasonsAnswers.set(ClaimReasonTypePage, ClaimReasonType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, claimReasonTypeRoute)

      val view = application.injector.instanceOf[ClaimReasonTypeView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ClaimReasonType.values.head), reasons, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to next page for a GET if not a multiple reason claim" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, claimReasonTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers))
          .build()

      val request =
        FakeRequest(POST, claimReasonTypeRoute)
          .withFormUrlEncodedBody(("value", reasons.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "redirect to the next page on submit if not a multiple reason claim" in {

      reset(mockSessionRepository)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, claimReasonTypeRoute)
          .withFormUrlEncodedBody(("value", reasons.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()

      verifyNoInteractions(mockSessionRepository)
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers)).build()

      val request =
        FakeRequest(POST, claimReasonTypeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[ClaimReasonTypeView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, reasons, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, claimReasonTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, claimReasonTypeRoute)
          .withFormUrlEncodedBody(("value", ClaimReasonType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
