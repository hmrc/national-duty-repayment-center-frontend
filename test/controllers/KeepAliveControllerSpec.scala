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
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with MockitoSugar {

  private def keepAliveRoute: String = controllers.routes.KeepAliveController.keepAlive().url

  "KeepAlive Controller" must {

    "persist existing answers to keep them alive" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      val result = route(application, FakeRequest(GET, keepAliveRoute)).value

      status(result) mustBe OK

      verify(mockSessionRepository).set(userAnswers)
    }
  }
}
