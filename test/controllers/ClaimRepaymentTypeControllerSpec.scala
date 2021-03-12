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
import forms.ClaimRepaymentTypeFormProvider
import models.{NormalMode, ClaimRepaymentType, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ClaimRepaymentTypePage
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ClaimRepaymentTypeView

import scala.concurrent.Future

class ClaimRepaymentTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val claimRepaymentTypeRoute = routes.ClaimRepaymentTypeController.onPageLoad(NormalMode).url

  val formProvider = new ClaimRepaymentTypeFormProvider()
  val form = formProvider()

  "ClaimRepaymentType Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, claimRepaymentTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimRepaymentTypeView]

      val backLink = routes.ReasonForOverpaymentController.onPageLoad(NormalMode)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, claimRepaymentTypeRoute)

      val view = application.injector.instanceOf[ClaimRepaymentTypeView]

      val result = route(application, request).value

      val backLink = routes.ReasonForOverpaymentController.onPageLoad(NormalMode)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ClaimRepaymentType.values.toSet), NormalMode, backLink)(fakeRequest, messages).toString

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
        FakeRequest(POST, claimRepaymentTypeRoute)
          .withFormUrlEncodedBody(("value[0]", ClaimRepaymentType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, claimRepaymentTypeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[ClaimRepaymentTypeView]

      val result = route(application, request).value

      val backLink = routes.ReasonForOverpaymentController.onPageLoad(NormalMode)

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, claimRepaymentTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, claimRepaymentTypeRoute)
          .withFormUrlEncodedBody(("value[0]", ClaimRepaymentType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
