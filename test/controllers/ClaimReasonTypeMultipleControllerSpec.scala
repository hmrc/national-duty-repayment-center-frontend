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
import forms.ClaimReasonTypeMultipleFormProvider
import models.ClaimReasonType
import models.ClaimReasonType.{CommodityCodeChange, CurrencyChanges}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.ClaimReasonTypeMultiplePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ClaimReasonTypeMultipleView

import scala.concurrent.Future

class ClaimReasonTypeMultipleControllerSpec extends SpecBase with MockitoSugar {

  lazy val claimReasonTypeMultipleRoute = routes.ClaimReasonTypeMultipleController.onPageLoad().url

  val formProvider = new ClaimReasonTypeMultipleFormProvider()
  val form         = formProvider()

  val reasons: Set[ClaimReasonType] = Set(CurrencyChanges, CommodityCodeChange)

  val multipleClaimReasonsAnswers =
    emptyUserAnswers.set(ClaimReasonTypeMultiplePage, reasons).success.value

  "ClaimReasonTypeMultiple Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, claimReasonTypeMultipleRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimReasonTypeMultipleView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers)).build()

      val request = FakeRequest(GET, claimReasonTypeMultipleRoute)

      val view = application.injector.instanceOf[ClaimReasonTypeMultipleView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(reasons), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers))
          .build()

      val request =
        FakeRequest(POST, claimReasonTypeMultipleRoute)
          .withFormUrlEncodedBody(("value[0]", ClaimReasonType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(multipleClaimReasonsAnswers)).build()

      val request =
        FakeRequest(POST, claimReasonTypeMultipleRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[ClaimReasonTypeMultipleView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, claimReasonTypeMultipleRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, claimReasonTypeMultipleRoute)
          .withFormUrlEncodedBody(("value", ClaimReasonType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
