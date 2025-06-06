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
import data.BarsTestData
import forms.BankDetailsFormProvider
import models.{BankDetails, ClaimantType, WhomToPay}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.{BankDetailsPage, ClaimantTypePage, WhomToPayPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.BankAccountReputationService
import views.html.BankDetailsView

import scala.concurrent.Future

class BankDetailsControllerSpec extends SpecBase with MockitoSugar with BarsTestData {

  val formProvider = new BankDetailsFormProvider()
  private val form = formProvider()

  private lazy val bankDetailsRoute = routes.BankDetailsController.onPageLoad().url

  private val userAnswersWithoutBankDetails =
    emptyUserAnswers.set(ClaimantTypePage, ClaimantType.Importer).success.value

  private val userAnswersWithBankDetails =
    userAnswersWithoutBankDetails.set(BankDetailsPage, BankDetails("name", "123456", "00123456")).success.value

  "BankDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutBankDetails)).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BankDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, Some(ClaimantType.Importer), None, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return correct view for a representative paying the importer" in {

      val application = applicationBuilder(userAnswers =
        Some(
          userAnswersWithoutBankDetails
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Importer).success.value
        )
      ).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BankDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, Some(ClaimantType.Representative), Some(WhomToPay.Importer), defaultBackLink)(
          request,
          messages
        ).toString

      application.stop()
    }

    "return correct view for a representative paying themselves" in {

      val application = applicationBuilder(userAnswers =
        Some(
          userAnswersWithoutBankDetails
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Representative).success.value
        )
      ).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BankDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, Some(ClaimantType.Representative), Some(WhomToPay.Representative), defaultBackLink)(
          request,
          messages
        ).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithBankDetails)).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val view = application.injector.instanceOf[BankDetailsView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(BankDetails("name", "123456", "00123456")), Some(ClaimantType.Importer), None, defaultBackLink)(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(barsSuccessResult)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutBankDetails))
          .overrides(bind[BankAccountReputationService].toInstance(mockBankAccountReputationService))
          .build()

      val request =
        FakeRequest(POST, bankDetailsRoute)
          .withFormUrlEncodedBody(("AccountName", "name"), ("SortCode", "123456"), ("AccountNumber", "00123456"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "redirect to the next page when sort code and account number has hyphens and spaces data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(barsSuccessResult)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutBankDetails))
          .overrides(bind[BankAccountReputationService].toInstance(mockBankAccountReputationService))
          .build()

      val request =
        FakeRequest(POST, bankDetailsRoute)
          .withFormUrlEncodedBody(("AccountName", "name"), ("SortCode", "12-34 56"), ("AccountNumber", "00-1234 56"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when BARS checks fail" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(
        barsSortcodeDoesNotExistResult
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutBankDetails))
          .overrides(bind[BankAccountReputationService].toInstance(mockBankAccountReputationService))
          .build()

      val request =
        FakeRequest(POST, bankDetailsRoute)
          .withFormUrlEncodedBody(("AccountName", "name"), ("SortCode", "123456"), ("AccountNumber", "00123456"))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      val view = application.injector.instanceOf[BankDetailsView]

      val errorForm =
        formProvider.processBarsResult(barsSortcodeDoesNotExistResult, BankDetails("name", "123456", "00123456")).get

      contentAsString(result) mustEqual
        view(errorForm, Some(ClaimantType.Importer), None, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(barsSuccessResult)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutBankDetails))
          .overrides(bind[BankAccountReputationService].toInstance(mockBankAccountReputationService))
          .build()

      val request =
        FakeRequest(POST, bankDetailsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[BankDetailsView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, Some(ClaimantType.Importer), None, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, bankDetailsRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
