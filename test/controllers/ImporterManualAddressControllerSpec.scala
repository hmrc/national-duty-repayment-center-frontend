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
import forms.ImporterManualAddressFormProvider
import models.{Address, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ImporterManualAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.{CountryOptions, FakeCountryOptions}
import views.html.ImporterManualAddressView

import scala.concurrent.Future

class ImporterManualAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ImporterManualAddressFormProvider()
  val form = formProvider()

  lazy val importerManualAddressRoute = routes.ImporterManualAddressController.onPageLoad(NormalMode).url

  "ImporterManualAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val mockCountryOptions = mock[CountryOptions]

      when(mockCountryOptions.options).thenReturn(FakeCountryOptions.fakeCountries)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).
        overrides(bind[CountryOptions].toInstance(mockCountryOptions)).build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ImporterManualAddressView]

      val backLink = routes.ImporterAddressController.onPageLoad(NormalMode)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, false, FakeCountryOptions.fakeCountries, backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val mockCountryOptions = mock[CountryOptions]

      when(mockCountryOptions.options).thenReturn(FakeCountryOptions.fakeCountries)

      val userAnswers = UserAnswers(userAnswersId).set(ImporterManualAddressPage, Address("address line 1", Some("address line 2"), "city", Some("Region"), "GB", Some("AA211AA"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).
        overrides(bind[CountryOptions].toInstance(mockCountryOptions)).build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val view = application.injector.instanceOf[ImporterManualAddressView]

      val result = route(application, request).value

      val backLink = routes.ImporterAddressController.onPageLoad(NormalMode)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Address("address line 1", Some("address line 2"), "city", Some("Region"), "GB", Some("AA211AA"))), NormalMode, false, FakeCountryOptions.fakeCountries, backLink)(fakeRequest, messages).toString

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
        FakeRequest(POST, importerManualAddressRoute)
          .withFormUrlEncodedBody(
            ("AddressLine1", "line 1"),
            ("AddressLine2", "line 2"),
            ("City", "postal City"),
            ("Region", "region"),
            ("CountryCode", "GB"),
            ("PostalCode", "AA1 1AA")
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val mockCountryOptions = mock[CountryOptions]

      when(mockCountryOptions.options).thenReturn(FakeCountryOptions.fakeCountries)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).
        overrides(bind[CountryOptions].toInstance(mockCountryOptions)).build()

      val request =
        FakeRequest(POST, importerManualAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ImporterManualAddressView]

      val result = route(application, request).value

      val backLink = routes.ImporterAddressController.onPageLoad(NormalMode)

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, false, FakeCountryOptions.fakeCountries, backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, importerManualAddressRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
