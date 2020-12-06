/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.{AddressSelectionFormProvider, AgentImporterAddressFormProvider, PostcodeFormProvider}
import models.responses.{AddressLookupResponseModel, Location, LookedUpAddress, LookedUpAddressWrapper, Uprn}
import models.results.UnexpectedResponseStatus
import models.{Address, NormalMode, PostcodeLookup, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentImporterAddressPage, AgentImporterPostcodePage}
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import views.html.{AgentImporterAddressConfirmationView, AgentImporterAddressView}

import scala.concurrent.Future

class AgentImporterAddressControllerSpec extends SpecBase with MockitoSugar {
  private lazy val agentImporterAddressPostcodeRoute = routes.AgentImporterAddressController.onPageLoad(NormalMode).url
  private lazy val agentImporterAddressSelectRoute = routes.AgentImporterAddressController.addressSelectSubmit.url
  private val postcodeForm = (new PostcodeFormProvider) ()
  private val addressForm = (new AgentImporterAddressFormProvider) ()
  private val selectionForm = (new AddressSelectionFormProvider) ()

  def onwardRoute: Call = Call("GET", "/national-duty-repayment-center/enter-agent-importer-address")

  def buildRequest(method: String, path: String): FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest(method, path)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  }

  "AgentImporterAddressController" must {
    "return OK and the correct view for a GET on the postcode page" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = buildRequest(GET, agentImporterAddressPostcodeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentImporterAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(postcodeForm, NormalMode)(request, messages).toString
      }
    }

    "populate the view correctly on a GET when the postcode question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId).set(AgentImporterPostcodePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = buildRequest(GET, agentImporterAddressPostcodeRoute)

        val view = application.injector.instanceOf[AgentImporterAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(postcodeForm.fill(PostcodeLookup("answer")), NormalMode)(request, messages).toString
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {

        val request = buildRequest(POST, agentImporterAddressPostcodeRoute)
          .withFormUrlEncodedBody(("foo", "bar"))

        val boundForm = postcodeForm.bind(Map("value" -> "12345678901234567890"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val expectedView = application.injector.instanceOf[AgentImporterAddressView]

        contentAsString(result) mustEqual
          expectedView(boundForm, NormalMode)(request, messages).toString
      }
    }

    "redirect to the next page when an address has been selected" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = buildRequest(POST, agentImporterAddressSelectRoute)
          .withFormUrlEncodedBody(
            "field-name" -> """{"line1":"Line1","town":"TOWN","postCode":"AA1 1AA"}"""
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors on the manual address entry page when invalid address data is submitted for selection" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = buildRequest(POST, agentImporterAddressSelectRoute)
          .withFormUrlEncodedBody(
            "field-name" -> """{}""",
            "address-postcode" -> "AA1 1AA"
          )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val expectedView = application.injector.instanceOf[AgentImporterAddressView]
        val boundForm = addressForm.bind(Map.empty[String, String])
        contentAsString(result) mustEqual
          expectedView(boundForm, NormalMode)(request, messages).toString
      }
    }

    "return a Bad Request and errors when nothing is selected" in {
      val addressLookupConnector = mock[AddressLookupConnector]
      val mockSessionRepository = mock[SessionRepository]

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
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(addressLookupConnector)
          )
          .build()

      running(application) {
        val request = buildRequest(POST, agentImporterAddressSelectRoute)
          .withFormUrlEncodedBody("address-postcode" -> "AA1 1AA")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val expectedView = application.injector.instanceOf[AgentImporterAddressConfirmationView]
        val expectedForm = selectionForm.withError("field-name", "Select the address or enter the address manually")
        val expectedSelectItems = Seq(
          SelectItem(
            text = "Line1  TOWN ",
            value = Some("""{"AddressLine1":"Line1","City":"TOWN","Region":"","CountryCode":"GB","postCode":"AA1 1AA"}"""))
        )

        contentAsString(result) mustEqual
          expectedView(expectedForm, PostcodeLookup("AA1 1AA"), expectedSelectItems, NormalMode)(request, messages).toString
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = buildRequest(GET, agentImporterAddressPostcodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = buildRequest(POST, agentImporterAddressPostcodeRoute)
          .withFormUrlEncodedBody("value" -> "answer")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
