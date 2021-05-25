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
import connectors.AddressLookupConnector
import forms.{AddressSelectionFormProvider, ImporterAddressFormProvider, PostcodeFormProvider}
import models.responses._
import models.{ClaimantType, NormalMode, PostcodeLookup, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ClaimantTypePage, ImporterPostcodePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import views.html.{ImporterAddressConfirmationView, ImporterAddressView}

import scala.concurrent.Future

class ImporterAddressControllerSpec extends SpecBase with MockitoSugar {
  private lazy val importerAddressPostcodeRoute = routes.ImporterAddressController.onPageLoad(NormalMode).url
  private lazy val importerAddressSelectRoute = routes.ImporterAddressController.addressSelectSubmit(NormalMode).url
  private val postcodeForm = (new PostcodeFormProvider) ()
  private val addressForm = (new ImporterAddressFormProvider) ()
  private val selectionForm = (new AddressSelectionFormProvider) ()

  def onwardRoute: Call = Call("GET", "/apply-for-repayment-of-import-duty-and-import-vat/enter-importer-address")

  "ImporterAddressController" must {
    "return OK and the correct view for a GET on the postcode page" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = buildRequest(GET, importerAddressPostcodeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ImporterAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(postcodeForm, NormalMode, false)(request, messages).toString
      }
    }

    "populate the view correctly on a GET when the postcode question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId).set(ImporterPostcodePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = buildRequest(GET, importerAddressPostcodeRoute)

        val view = application.injector.instanceOf[ImporterAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(postcodeForm.fill(PostcodeLookup("answer")), NormalMode, false)(request, messages).toString
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {

        val request = buildRequest(POST, importerAddressPostcodeRoute)
          .withFormUrlEncodedBody(("foo", "bar"))

        val boundForm = postcodeForm.bind(Map("value" -> "12345678901234567890"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val expectedView = application.injector.instanceOf[ImporterAddressView]

        contentAsString(result) mustEqual
          expectedView(boundForm, NormalMode, false)(request, messages).toString
      }
    }

    "redirect to the next page when an address has been selected" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request = buildRequest(POST, importerAddressSelectRoute)
          .withFormUrlEncodedBody(
            "field-name" -> """{"line1":"Line1","town":"TOWN","postCode":"AA1 1AA"}"""
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

      "return a Bad Request and errors on the manual address entry page when invalid address data is submitted for selection" in {

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
            )
            .build()

        running(application) {
          val request = buildRequest(POST, importerAddressSelectRoute)
            .withFormUrlEncodedBody(
              "field-name" -> """{}""",
              "address-postcode" -> "AA1 1AA"
            )

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          val expectedView = application.injector.instanceOf[ImporterAddressView]
          val boundForm = addressForm.bind(Map.empty[String, String])
          contentAsString(result) mustEqual
            expectedView(boundForm, NormalMode, false)(request, messages).toString
        }
      }

      "return a Bad Request and errors when nothing is selected" in {
        val addressLookupConnector = mock[AddressLookupConnector]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val addresses = Seq(
          LookedUpAddressWrapper("1", Uprn(1234567890L), LookedUpAddress(Seq("Line1"), "TOWN", None, "AA1 1AA"), "LA", Some(Location(0, 0)))
        )

        when(addressLookupConnector.addressLookup(any())(any()))
          .thenReturn(Future.successful(Right(AddressLookupResponseModel(addresses))))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[AddressLookupConnector].toInstance(addressLookupConnector)
            )
            .build()

        running(application) {
          val request = buildRequest(POST, importerAddressSelectRoute)
            .withFormUrlEncodedBody("address-postcode" -> "AA1 1AA")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val expectedView = application.injector.instanceOf[ImporterAddressConfirmationView]
          val expectedForm = selectionForm.withError("field-name", "Select the address or enter the address manually")
          val expectedSelectItems = Seq(
            SelectItem(
              text = "Line1  TOWN ",
              value = Some("""{"AddressLine1":"Line1","City":"TOWN","CountryCode":"GB","PostalCode":"AA1 1AA"}"""))
          )

          contentAsString(result) mustEqual
            expectedView(expectedForm, PostcodeLookup("AA1 1AA"), expectedSelectItems, NormalMode, false)(request, messages).toString
        }
      }

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(GET, importerAddressPostcodeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
        }
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(POST, importerAddressPostcodeRoute)
            .withFormUrlEncodedBody("value" -> "answer")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
        }
      }

      "return header as 'What is the importer's address?' when user is Representative" in {
        val userAnswers = UserAnswers(userAnswersId).set(ClaimantTypePage, ClaimantType.Representative).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = buildRequest(GET, importerAddressPostcodeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ImporterAddressView]

          contentAsString(result) mustEqual
            view(postcodeForm, NormalMode, false)(request, messages).toString
        }
      }

        "return header as 'What is your address?' when user is Importer" in {
          val userAnswers = UserAnswers(userAnswersId).set(ClaimantTypePage, ClaimantType.Importer).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
          running(application) {
            val request = buildRequest(GET, importerAddressPostcodeRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ImporterAddressView]

            contentAsString(result) mustEqual
              view(postcodeForm, NormalMode, true)(request, messages).toString
          }
        }

    "return header as 'Select their' when user is Representative" in {
      val userAnswers = UserAnswers(userAnswersId).set(ClaimantTypePage, ClaimantType.Representative).success.value
      val addressLookupConnector = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      val addresses = Seq(
        LookedUpAddressWrapper("1", Uprn(1234567890L), LookedUpAddress(Seq("Line1"), "TOWN", None, "AA1 1AA"), "LA", Some(Location(0, 0)))
      )

      when(addressLookupConnector.addressLookup(any())(any()))
        .thenReturn(Future.successful(Right(AddressLookupResponseModel(addresses))))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[AddressLookupConnector].toInstance(addressLookupConnector)
          )
          .build()

      running(application) {
        val request = buildRequest(POST, importerAddressSelectRoute)
          .withFormUrlEncodedBody("address-postcode" -> "AA1 1AA")

        val result = route(application, request).value

        val view = application.injector.instanceOf[ImporterAddressConfirmationView]

        val expectedForm = selectionForm.withError("field-name", "Select the address or enter the address manually")
        val expectedSelectItems = Seq(
          SelectItem(
            text = "Line1  TOWN ",
            value = Some("""{"AddressLine1":"Line1","City":"TOWN","CountryCode":"GB","PostalCode":"AA1 1AA"}"""))
        )

        contentAsString(result) mustEqual
          view(expectedForm, PostcodeLookup("AA1 1AA"), expectedSelectItems, NormalMode, false)(request, messages).toString
      }
    }

    "return header as 'Select your address' when user is Importer" in {
      val userAnswers = UserAnswers(userAnswersId).set(ClaimantTypePage, ClaimantType.Importer).success.value
      val addressLookupConnector = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      val addresses = Seq(
        LookedUpAddressWrapper("1", Uprn(1234567890L), LookedUpAddress(Seq("Line1"), "TOWN", None, "AA1 1AA"), "LA", Some(Location(0, 0)))
      )

      when(addressLookupConnector.addressLookup(any())(any()))
        .thenReturn(Future.successful(Right(AddressLookupResponseModel(addresses))))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[AddressLookupConnector].toInstance(addressLookupConnector)
          )
          .build()

      running(application) {
        val request = buildRequest(POST, importerAddressSelectRoute)
          .withFormUrlEncodedBody("address-postcode" -> "AA1 1AA")

        val result = route(application, request).value

        val view = application.injector.instanceOf[ImporterAddressConfirmationView]

        val expectedForm = selectionForm.withError("field-name", "Select the address or enter the address manually")
        val expectedSelectItems = Seq(
          SelectItem(
            text = "Line1  TOWN ",
            value = Some("""{"AddressLine1":"Line1","City":"TOWN","CountryCode":"GB","PostalCode":"AA1 1AA"}"""))
        )

        contentAsString(result) mustEqual
          view(expectedForm, PostcodeLookup("AA1 1AA"), expectedSelectItems, NormalMode, true)(request, messages).toString
      }
    }
    }
}
