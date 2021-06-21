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
import data.BarsTestData
import forms.BankDetailsFormProvider
import models.{BankDetails, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.BankDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.BankAccountReputationService
import views.html.BankDetailsView

import scala.concurrent.Future

class BankDetailsControllerSpec extends SpecBase with MockitoSugar with BarsTestData {

  val formProvider = new BankDetailsFormProvider()
  val form         = formProvider()

  lazy val bankDetailsRoute = routes.BankDetailsController.onPageLoad().url

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      BankDetailsPage.toString -> Json.obj(
        "AccountName"   -> "name",
        "SortCode"      -> "123456",
        "AccountNumber" -> "00123456"
      )
    )
  )

  "BankDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BankDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, bankDetailsRoute)

      val view = application.injector.instanceOf[BankDetailsView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(BankDetails("name", "123456", "00123456")), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(barsSuccessResult)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
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

    "return a Bad Request and errors when BARS checks fail" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(
        barsSortcodeDoesNotExistResult
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
        view(errorForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val mockBankAccountReputationService = mock[BankAccountReputationService]
      when(mockBankAccountReputationService.validate(any())(any())) thenReturn Future.successful(barsSuccessResult)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
        view(boundForm, defaultBackLink)(request, messages).toString

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
