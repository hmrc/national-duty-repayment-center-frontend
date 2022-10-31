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

import java.util.UUID

import base.SpecBase
import data.TestData
import data.TestData.addressLookupConfirmation
import forms.AgentImporterManualAddressFormProvider
import models.addresslookup.AddressLookupOnRamp
import models.{Address, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentImporterAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressLookupService, CountryService}
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import views.html.AgentImporterManualAddressView

import scala.concurrent.Future

class AgentImporterAddressFrontendControllerSpec extends SpecBase with MockitoSugar {

  implicit val countriesService = TestData.testCountryService
  val formProvider              = new AgentImporterManualAddressFormProvider()
  val form                      = formProvider()

  lazy val agentImporterManualAddressRoute = routes.AgentImporterAddressFrontendController.onPageLoad().url
  lazy val agentImporterChangeAddressRoute = routes.AgentImporterAddressFrontendController.onChange().url
  lazy val agentImporterUpdateAddressRoute = routes.AgentImporterAddressFrontendController.onUpdate(Some("id")).url

  "AgentImporterAddressFrontendController" must {

    "redirects to address lookup when there is no address in cache" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AgentImporterAddressFrontendController.onChange().url

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
        .overrides(bind[CountryService].toInstance(countriesService))
        .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
        .build()

      val request = FakeRequest(GET, agentImporterChangeAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "http://localhost/AddressLookupReturnedRedirect"

      application.stop()

      // make sure "id" query param is not part of the callback url
      callBackUrlCaptor.getValue must not include "id="
    }

    "populate the view correctly on a GET when the question has previously been answered for UK address" in {

      populateExistingAddresss(addressUk)

    }

    "populate the view correctly on a GET when the question has previously been answered for non-UK address" in {

      populateExistingAddresss(addressInternational)

    }

    def populateExistingAddresss(address: Address) = {
      val userAnswers = UserAnswers(userIdentification).set(AgentImporterAddressPage, address).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val view = application.injector.instanceOf[AgentImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(address), defaultBackLink, countriesService.selectItems())(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
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

    "retain international postcode when submitted" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CountryService].toInstance(countriesService))
          .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(
            ("AddressLine1", "line 1"),
            ("City", "postal City"),
            ("CountryCode", "FR"),
            ("PostalCode", "FR123456")
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      application.stop()

      persistedAnswers.getValue.get(AgentImporterAddressPage).flatMap(_.PostalCode) mustBe Some("FR123456")
    }

    "retain audit reference when same address is submitted manually" in {

      val userAnswers = UserAnswers(userIdentification).set(AgentImporterAddressPage, addressUk).success.value

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CountryService].toInstance(countriesService))
          .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(
            ("AddressLine1", addressUk.AddressLine1),
            ("AddressLine2", addressUk.AddressLine2.getOrElse("")),
            ("City", addressUk.City),
            ("Region", addressUk.Region.getOrElse("")),
            ("CountryCode", addressUk.Country.code),
            ("PostalCode", addressUk.PostalCode.getOrElse("")),
            ("auditRef", addressUk.auditRef.getOrElse(""))
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()

      persistedAnswers.getValue.get(AgentImporterAddressPage) mustBe Some(addressUk)
    }

    "clear audit reference when changed address is submitted manually" in {

      val userAnswers = UserAnswers(userIdentification).set(AgentImporterAddressPage, addressUk).success.value

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CountryService].toInstance(countriesService))
          .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(
            ("AddressLine1", addressUk.AddressLine1),
            ("AddressLine2", addressUk.AddressLine2.getOrElse("")),
            ("City", "Different"),
            ("Region", addressUk.Region.getOrElse("")),
            ("CountryCode", addressUk.Country.code),
            ("PostalCode", addressUk.PostalCode.getOrElse("")),
            ("auditRef", addressUk.auditRef.getOrElse(""))
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()

      persistedAnswers.getValue.get(AgentImporterAddressPage) mustBe Some(
        addressUk.copy(City = "Different", auditRef = None)
      )
    }

    "persist international address inc. postal code with audit ref and redirect to next page when valid data returned from address lookup" in {
      val mockAddressLookupService = mock[AddressLookupService]

      val auditRef = UUID.randomUUID().toString

      when(mockAddressLookupService.retrieveAddress(any())(any(), any())).thenReturn(
        Future.successful(addressLookupConfirmation(auditRef, "Berlin", Some("352456"), "DE"))
      )

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CountryService].toInstance(countriesService))
          .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      val request = FakeRequest(GET, agentImporterUpdateAddressRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      persistedAnswers.getValue.get(AgentImporterAddressPage).flatMap(_.auditRef) mustBe Some(auditRef)

      persistedAnswers.getValue.get(AgentImporterAddressPage).flatMap(_.PostalCode) mustBe Some("352456")

    }

    "show error page when invalid data returned from address lookup" in {
      val mockAddressLookupService = mock[AddressLookupService]

      val invalidCity = "Southampton" * 10
      when(mockAddressLookupService.retrieveAddress(any())(any(), any())).thenReturn(
        Future.successful(
          addressLookupConfirmation(
            city = invalidCity, // too long
            postCode = None,    // missing for GB
            countryCode = "GB"
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[CountryService].toInstance(countriesService))
          .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      val request = FakeRequest(GET, agentImporterUpdateAddressRoute)
      val result  = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) must include("City must be 64 characters or less")
      contentAsString(result) must include("Enter a UK postcode")
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AgentImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink, Seq(SelectItem(text = "United Kingdom", value = Some("GB"))))(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryService].toInstance(countriesService))
        .build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
