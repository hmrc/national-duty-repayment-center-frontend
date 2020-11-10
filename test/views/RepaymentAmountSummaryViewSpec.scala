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

package views

import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}
import views.behaviours.ViewBehaviours
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryViewSpec extends ViewBehaviours {

  def answersViewModel = Seq(
    AnswerSection(Some("fake heading"), Seq(
      AnswerRow(Html("label1"), Html("answer1"), Some("changeurl1")),
      AnswerRow(Html("label2"), Html("answer2"), Some("changeurl2")),
      AnswerRow(Html("label3"), Html("answer3"), Some("changeurl3"))
    ))
  )

  "RepaymentAmountSummary view" must {

    val view = viewFor[RepaymentAmountSummaryView](Some(emptyUserAnswers))

    val applyView = view.apply(answersViewModel)(fakeRequest, messages)

    behave like normalPage(applyView, "repaymentAmountSummary")

    behave like pageWithBackLink(applyView)
  }
}
