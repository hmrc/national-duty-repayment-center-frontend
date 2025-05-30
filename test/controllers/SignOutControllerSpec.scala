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
import org.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SignOutControllerSpec extends SpecBase with MockitoSugar {

  private def signOutRoute: String = controllers.routes.SignOutController.signOut().url
  private val application          = applicationBuilder().build()

  "SignOut Controller" must {

    "Sign in using Government Gateway page" in {

      when(mockAppConfig.signOutUrl).thenReturn(frontendAppConfig.signOutUrl)
      val result = route(application, FakeRequest(GET, signOutRoute)).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.feedbackSurvey)
    }
  }
}
