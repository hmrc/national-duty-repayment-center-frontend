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
import forms.VATPaidFormProvider
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.{ClaimRepaymentTypePage, NumberOfEntriesTypePage, VATPaidPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.VATPaidView

import scala.concurrent.Future

class VATPaidControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new VATPaidFormProvider()
  val form         = formProvider()

  lazy val vATPaidRoute = routes.VATPaidController.onPageLoad().url

  private val userAnswers = UserAnswers(
    userAnswersId,
    None,
    Json.obj(VATPaidPage.toString -> Json.obj("ActualPaidAmount" -> "100.00", "ShouldHavePaidAmount" -> "50.00"))
  )

  "VATPaid Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userIdentification).set(
        ClaimRepaymentTypePage,
        ClaimRepaymentType.values.toSet
      ).success.value.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, vATPaidRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[VATPaidView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink, true)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersFull = userAnswers.set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value
        .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersFull)).build()

      val request = FakeRequest(GET, vATPaidRoute)

      val view = application.injector.instanceOf[VATPaidView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(RepaymentAmounts("100.00", "50.00")), defaultBackLink, false)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, vATPaidRoute)
          .withFormUrlEncodedBody("ActualPaidAmount" -> "100.00", "ShouldHavePaidAmount" -> "50.00")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers =
        UserAnswers(userIdentification).set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value
          .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, vATPaidRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[VATPaidView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink, false)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, vATPaidRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, vATPaidRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
