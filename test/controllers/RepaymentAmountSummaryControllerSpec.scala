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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryControllerSpec extends SpecBase {

  def answersViewModel = Seq(
    AnswerSection(Some("fake heading"), Seq(
      AnswerRow(Html("label1"), Html("answer1"), Some("changeurl1")),
      AnswerRow(Html("label2"), Html("answer2"), Some("changeurl2")),
      AnswerRow(Html("label3"), Html("answer3"), Some("changeurl3"))
    ))
  )

  "RepaymentAmountSummary Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.RepaymentAmountSummaryController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RepaymentAmountSummaryView]

      //status(result) mustEqual OK

      //contentAsString(result) mustEqual
        //view(answersViewModel)(fakeRequest, messages).toString

      application.stop()
    }
  }
}
