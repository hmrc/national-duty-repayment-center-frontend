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
import data.TestData
import forms.ImporterManualAddressFormProvider
import models.addresslookup.AddressLookupOnRamp
import models.{Address, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ImporterAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressLookupService, CountryService}
import views.html.ImporterManualAddressView

import scala.concurrent.Future

class ImporterAddressFrontendControllerSpec extends SpecBase with MockitoSugar {

  implicit val testCountryService = TestData.testCountryService
  val formProvider                = new ImporterManualAddressFormProvider()
  val form                        = formProvider()

  lazy val importerManualAddressRoute = routes.ImporterAddressFrontendController.onPageLoad().url
  lazy val importerChangeAddressRoute = routes.ImporterAddressFrontendController.onChange().url

  "ImporterAddressFrontendController" must {

    "redirects to address lookup when there is no address in cache" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(testCountryService))
        .build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ImporterAddressFrontendController.onChange().url

      application.stop()
    }

    "calls address lookup initialise with correct url" in {

      val mockAddressLookupService = mock[AddressLookupService]

      val callBackUrlCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(
        mockAddressLookupService.initialiseJourney(
          callBackUrlCaptor.capture(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any(), any())
      ).thenReturn(Future.successful(AddressLookupOnRamp("http://localhost/AddressLookupReturnedRedirect")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(testCountryService))
        .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
        .build()

      val request = FakeRequest(GET, importerChangeAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "http://localhost/AddressLookupReturnedRedirect"

      application.stop()

      // make sure "id" query param is not part of the callback url
      callBackUrlCaptor.getValue must not include "id="
    }

    "populate the view correctly on a GET when the question has previously been answered for UK address" in {

      val userAnswers = UserAnswers(userAnswersId).set(ImporterAddressPage, addressUk).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CountryService].toInstance(testCountryService))
        .build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val view = application.injector.instanceOf[ImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(addressUk), defaultBackLink, false, testCountryService.selectItems())(request, messages).toString

      application.stop()
    }

    def populateExistingAddresss(address: Address) = {}

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CountryService].toInstance(testCountryService))
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
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(testCountryService))
        .build()

      val request =
        FakeRequest(POST, importerManualAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink, false, testCountryService.selectItems())(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryService].toInstance(testCountryService))
        .build()

      val request = FakeRequest(GET, importerManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryService].toInstance(testCountryService))
        .build()

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
