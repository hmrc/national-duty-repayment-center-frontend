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
import forms.CreateOrAmendCaseFormProvider
import models.{CreateOrAmendCase, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CreateOrAmendCasePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.CreateOrAmendCaseView

import scala.concurrent.Future

class CreateOrAmendCaseControllerSpec extends SpecBase with MockitoSugar {

  lazy val createOrAmendCaseRoute = routes.CreateOrAmendCaseController.onPageLoad.url

  val formProvider = new CreateOrAmendCaseFormProvider()
  val form         = formProvider()

  "CreateOrAmendCase Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, createOrAmendCaseRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CreateOrAmendCaseView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(CreateOrAmendCasePage, CreateOrAmendCase.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, createOrAmendCaseRoute)

      val view = application.injector.instanceOf[CreateOrAmendCaseView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(CreateOrAmendCase.values.head))(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(CreateOrAmendCasePage, CreateOrAmendCase.values.head).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSessionRepository.resetData(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      val request =
        FakeRequest(POST, createOrAmendCaseRoute)
          .withFormUrlEncodedBody(("value", CreateOrAmendCase.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, createOrAmendCaseRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[CreateOrAmendCaseView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }
  }
}
