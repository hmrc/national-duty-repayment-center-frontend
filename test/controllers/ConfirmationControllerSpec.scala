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
import data.TestData.populateUserAnswersWithCMAPaymentMethod
import models.UserAnswers
import org.mockito.ArgumentCaptor
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.ClaimIdQuery
import views.html.{ClaimSummaryView, ConfirmationView}

import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase {

  "ConfirmationController for confirmation view" must {

    "return OK and the correct view for a GET when claimId can be retrieved from user answers" in {

      val claimId = "1"

      val answers = emptyUserAnswers
        .set(ClaimIdQuery, claimId).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmationView]

        status(result) mustEqual OK

        val checkYourAnswersHelper = cyaFactory.instance(answers)

        contentAsString(result) mustEqual
          view(claimId, checkYourAnswersHelper.getCreateConfirmationSections)(request, messages).toString

      }
    }

    "redirect to start for a GET when claimId cannot be retrieved from user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }
  }

  "ConfirmationController for summary view" must {

    "return OK and the correct view for a GET when claimId can be retrieved from user answers" in {

      val claimId = "1"

      val answers = emptyUserAnswers
        .set(ClaimIdQuery, claimId).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ConfirmationController.onSummary().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimSummaryView]

        status(result) mustEqual OK

        val checkYourAnswersHelper = cyaFactory.instance(answers)

        contentAsString(result) mustEqual
          view(checkYourAnswersHelper.getCreateConfirmationSections, "confirmation.summary.title")(
            request,
            messages
          ).toString

      }
    }

    "redirect to start for a GET when claimId cannot be retrieved from user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ConfirmationController.onSummary().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }
  }

  "ConfirmationController for start new application" must {

    "clear user answers and redirect to start page" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val userAnswers = populateUserAnswersWithCMAPaymentMethod(emptyUserAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ConfirmationController.onStartNewApplication().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }

      persistedAnswers.getValue.data mustBe Json.obj()
      persistedAnswers.getValue.changePage mustBe None
    }
  }
}
