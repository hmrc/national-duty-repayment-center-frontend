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
import models.AmendCaseResponseType
import models.AmendCaseResponseType.FurtherInformation
import navigation.NavigatorBack
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import pages.{AmendCaseResponseTypePage, ReferenceNumberPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{ApplicationClosedView, ApplicationNotFoundView}

import scala.concurrent.Future

class AmendErrorControllerSpec extends SpecBase with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  override protected def afterEach(): Unit = {
    reset(mockSessionRepository)
    super.afterEach()
  }

  def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  val backLink = NavigatorBack(Some(routes.ReferenceNumberController.onPageLoad()))

  "Amend Error Controller" must {

    "return OK and the correct view for 'case not found'" in {
      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
      val userAnswers = emptyUserAnswers
        .set(ReferenceNumberPage, "CASE1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendErrorController.onNotFound().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ApplicationNotFoundView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view("CASE1234")(request, messages).toString

      contentAsString(result) must include(frontendAppConfig.emails.customsAccountingRepayments)

      application.stop()
    }

    "return NotFound when the case reference is missing onNotFound" in {
      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendErrorController.onNotFound().url)

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND

      application.stop()
    }

    "return OK and the correct view for 'case closed'" in {
      val values: Seq[AmendCaseResponseType] = Seq(FurtherInformation)
      val userAnswers = emptyUserAnswers
        .set(ReferenceNumberPage, "CASE1234").success.value
        .set(AmendCaseResponseTypePage, values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendErrorController.onClosed().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ApplicationClosedView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view("CASE1234")(request, messages).toString

      contentAsString(result) must include(frontendAppConfig.emails.customsAccountingRepayments)

      application.stop()
    }

    "return NotFound when the case reference is missing onClosed" in {
      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.AmendErrorController.onClosed().url)

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND

      application.stop()
    }

  }
}
