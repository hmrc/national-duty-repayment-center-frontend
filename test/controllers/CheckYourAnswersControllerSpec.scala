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
import data.TestData._
import models.UserAnswers
import navigation.CreateNavigatorImpl
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import pages.{CheckYourAnswersPage, ClaimRepaymentTypePage, ImporterHasEoriPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.ClaimIdQuery
import views.html.{CheckYourAnswersView, CheckYourMissingAnswersView}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  override protected def afterEach(): Unit = {
    reset(mockSessionRepository)
    super.afterEach()
  }

  "Check Your Answers Controller" must {

    when(mockCreateNavigator.firstMissingAnswer(any())).thenReturn(None)

    "return OK and the correct view for an Importer Journey GET" in {
      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers)

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

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

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

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

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

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

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

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

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

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

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(checkYourAnswersHelper.getCheckYourAnswerSections, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "display 'missing answers' view and not update cached data" in {

      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers).remove(ImporterHasEoriPage).get

      val navigator = injector.instanceOf[CreateNavigatorImpl]

      val application = applicationBuilder(userAnswers = Some(userAnswers), createNavigator = navigator).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val view = application.injector.instanceOf[CheckYourMissingAnswersView]

      val checkYourAnswersHelper = cyaFactory.instance(userAnswers)

      contentAsString(result) mustEqual
        view(
          checkYourAnswersHelper.getCheckYourAnswerSections,
          navigator.previousPage(CheckYourAnswersPage, userAnswers)
        )(request, messages).toString

      application.stop()

      verifyNoInteractions(mockSessionRepository)
    }

    "redirect to missing answer on resolve" in {

      val userAnswers = populateUserAnswersWithImporterInformation(emptyUserAnswers).remove(ImporterHasEoriPage).get

      val navigator = injector.instanceOf[CreateNavigatorImpl]

      val application = applicationBuilder(userAnswers = Some(userAnswers), createNavigator = navigator).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onResolve().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ImporterHasEoriController.onPageLoad().url

      application.stop()

      verifyNoInteractions(mockSessionRepository)
    }

    "reload page on resolve if no answer is missing" in {

      val userAnswers = emptyUserAnswers

      when(mockCreateNavigator.firstMissingAnswer(any())).thenReturn(None)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), createNavigator = mockCreateNavigator).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onResolve().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

      application.stop()

      verifyNoInteractions(mockSessionRepository)
    }

    "not redirect and clear changePage when returning from changing repayment type" in {
      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)
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

      persistedAnswers.getValue.changePage mustBe None
    }

    "persist 'change page' and redirect onChange" in {

      val userAnswers = emptyUserAnswers

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      when(mockCreateNavigator.gotoPage(any())).thenReturn(routes.BankDetailsController.onPageLoad())
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), createNavigator = mockCreateNavigator).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onChange("bankPage").url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BankDetailsController.onPageLoad().url

      application.stop()

      persistedAnswers.getValue.changePage mustBe Some("bankPage")
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to start for a GET if claim submitted" in {

      when(mockSessionRepository.resetData(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(ClaimIdQuery, "1234").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

      application.stop()
    }

    "submit claim and update state onSubmit" in {

      val userAnswers = emptyUserAnswers

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      when(mockCreateNavigator.nextPage(any(), any())).thenReturn(routes.ConfirmationController.onPageLoad())
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), createNavigator = mockCreateNavigator).build()

      when(mockClaimService.submitClaim(any())(any())).thenReturn(Future.successful("claimID"))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad().url

      application.stop()

      persistedAnswers.getValue.get(ClaimIdQuery) mustBe Some("claimID")
    }
  }
}
