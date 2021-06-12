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
import data.TestData._
import navigation.CreateNavigatorImpl
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.{ClaimRepaymentTypePage, ImporterHasEoriPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" must {

    when(mockSessionRepository.clearChangePage(any())).thenReturn(Future.successful(true))
    when(mockCreateNavigator.firstMissingAnswer(any())).thenReturn(None)

    "return OK and the correct view for an Importer Journey GET" in {

      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for an Importer Journey UKCustomsRegulation GET" in {

      val userAnswers = populateUserAnswersWithImporterUKCustomsRegulationInformation(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for Representative Single BACS Journey GET" in {

      val userAnswers = populateUserAnswersWithRepresentativeSingleBACSJourney(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for Representative Single Paying Representative Journey GET" in {

      val userAnswers = populateUserAnswersWithRepresentativeSinglePayingRepresentativeJourney(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for Representative Single CMA Journey GET" in {

      val userAnswers = populateUserAnswersWithRepresentativeSingleCMAJourney(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for Representative Multiple Journey GET" in {

      val userAnswers = populateUserAnswersWithRepresentativeMultipleJourney(emptyUserAnswers)

      val checkYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to first missing answer" in {

      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers).remove(ImporterHasEoriPage).get

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        createNavigator = injector.instanceOf[CreateNavigatorImpl]
      ).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ImporterHasEoriController.onPageLoad().url

      application.stop()
    }

    "not redirect when returning from changing repayment type" in {

      val userAnswers =
        populateUserAnswersWithImporterInformation(emptyUserAnswers).copy(changePage = Some(ClaimRepaymentTypePage))

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        createNavigator = injector.instanceOf[CreateNavigatorImpl]
      ).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
