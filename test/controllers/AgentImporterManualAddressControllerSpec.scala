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
import forms.AgentImporterManualAddressFormProvider
import models.{Address, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentImporterManualAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import views.html.AgentImporterManualAddressView

import scala.concurrent.Future

class AgentImporterManualAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AgentImporterManualAddressFormProvider()
  val form = formProvider()

  lazy val agentImporterManualAddressRoute = routes.AgentImporterManualAddressController.onPageLoad(NormalMode).url

  "AgentImporterManualAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val backLink = routes.AgentImporterAddressController.onPageLoad(NormalMode)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AgentImporterManualAddressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, Seq(SelectItem(text = "United Kingdom", value = Some("GB"))), backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val backLink = routes.AgentImporterAddressController.onPageLoad(NormalMode)

      val userAnswers = UserAnswers(userAnswersId).set(
        AgentImporterManualAddressPage,
        Address("address line 1", Some("address line 2"), "city", Some("Region"), "GB", Some("AA211AA"))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val view = application.injector.instanceOf[AgentImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(
          Address("address line 1", Some("address line 2"), "city", Some("Region"), "GB", Some("AA211AA"))
        ), NormalMode, Seq(SelectItem(text = "United Kingdom", value = Some("GB"))), backLink)(fakeRequest, messages).toString

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
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val backLink = routes.AgentImporterAddressController.onPageLoad(NormalMode)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))build()

      val request =
        FakeRequest(POST, agentImporterManualAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AgentImporterManualAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, Seq(SelectItem(text = "United Kingdom", value = Some("GB"))), backLink)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, agentImporterManualAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

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
