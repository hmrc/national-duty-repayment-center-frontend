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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SessionExpiredView

import java.net.URLEncoder

class SessionExpiredControllerSpec extends SpecBase {

  "SessionExpired Controller" must {

    "return OK and the correct redirect URL for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.SessionExpiredController.onPageLoad().url)

      val result = route(application, request).value

      val feedbackURLEncoded: String = URLEncoder.encode(routes.SessionExpiredController.showView().url, "UTF-8")

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual s"${frontendAppConfig.signOutUrl}?continue=$feedbackURLEncoded"

      application.stop()
    }
  }
}
