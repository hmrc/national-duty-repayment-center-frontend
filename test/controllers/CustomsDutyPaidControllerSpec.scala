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
import forms.CustomsDutyPaidFormProvider
import models.{ClaimRepaymentType, NormalMode, NumberOfEntriesType, RepaymentAmounts, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ClaimRepaymentTypePage, CustomsDutyPaidPage, NumberOfEntriesTypePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.CustomsDutyPaidView

import scala.concurrent.Future

class CustomsDutyPaidControllerSpec extends SpecBase with MockitoSugar {

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      CustomsDutyPaidPage.toString -> Json.obj(
        "ActualPaidAmount"   -> "100.00",
        "ShouldHavePaidAmount"      -> "50.00"
      )
    )
  )

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CustomsDutyPaidFormProvider()
  val form = formProvider()

  lazy val CustomsDutyPaidRoute = routes.CustomsDutyPaidController.onPageLoad(NormalMode).url

  "customsDutyPaid Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswersFull = UserAnswers(userAnswersId)
        .set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value
        .set(NumberOfEntriesTypePage, NumberOfEntriesType.Multiple).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersFull)).build()

      val request = FakeRequest(GET, CustomsDutyPaidRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CustomsDutyPaidView]

      val backLink = routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, backLink, false)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersFull = userAnswers
        .set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value
        .set(NumberOfEntriesTypePage, NumberOfEntriesType.Multiple).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersFull)).build()

      val request = FakeRequest(GET, CustomsDutyPaidRoute)

      val view = application.injector.instanceOf[CustomsDutyPaidView]

      val backLink = routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(RepaymentAmounts("100.00", "50.00")), NormalMode, backLink, false)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, CustomsDutyPaidRoute)
          .withFormUrlEncodedBody(
            ("ActualPaidAmount", "100.00"),
            ("ShouldHavePaidAmount", "50.00")
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswersFull = UserAnswers(userAnswersId)
        .set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value
        .set(NumberOfEntriesTypePage, NumberOfEntriesType.Multiple).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersFull)).build()

      val request =
        FakeRequest(POST, CustomsDutyPaidRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[CustomsDutyPaidView]

      val backLink = routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, backLink, false)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, CustomsDutyPaidRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, CustomsDutyPaidRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
