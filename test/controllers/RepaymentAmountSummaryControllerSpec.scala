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
import models.{ClaimRepaymentType, UserAnswers}
import pages.{ClaimRepaymentTypePage, CustomsDutyPaidPage, OtherDutiesPaidPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryControllerSpec extends SpecBase {

  implicit class Improvements(s: Double) {
    def format2d = "%.2f".format(s)
  }

  def answersViewModel = Seq(
    AnswerSection(
      Some("Customs Duty"),
      Seq(
        AnswerRow(
          Html("Amount that was paid"),
          Html("£0.00"),
          rowClass = Some("govuk-summary-list__row--no-border"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-bottom-0"),
          valueClass = Some("govuk-!-padding-bottom-0")
        ),
        AnswerRow(
          Html("Amount that should have been paid"),
          Html("£0.00"),
          rowClass = Some("govuk-!-padding-top-0"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-top-0"),
          valueClass = Some("govuk-!-padding-top-0")
        ),
        AnswerRow(
          Html("Total Customs Duty return amount"),
          Html("<span class=\"govuk-!-font-weight-bold\">£0.00</span>"),
          Some("/apply-for-repayment-of-import-duty-and-import-vat/check-answers/change/customsDutyPaid"),
          Some("customs-duty-overpayment"),
          keyClass = Some("govuk-!-width-three-quarters")
        )
      )
    ),
    AnswerSection(
      Some("VAT"),
      Seq(
        AnswerRow(
          Html("Amount that was paid"),
          Html("£0.00"),
          rowClass = Some("govuk-summary-list__row--no-border"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-bottom-0"),
          valueClass = Some("govuk-!-padding-bottom-0")
        ),
        AnswerRow(
          Html("Amount that should have been paid"),
          Html("£0.00"),
          rowClass = Some("govuk-!-padding-top-0"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-top-0"),
          valueClass = Some("govuk-!-padding-top-0")
        ),
        AnswerRow(
          Html("Total VAT return amount"),
          Html("<span class=\"govuk-!-font-weight-bold\">£0.00</span>"),
          Some("/apply-for-repayment-of-import-duty-and-import-vat/check-answers/change/vATPaid"),
          Some("change-import-vat-overpayment"),
          keyClass = Some("govuk-!-width-three-quarters")
        )
      )
    ),
    AnswerSection(
      Some("Other duties"),
      Seq(
        AnswerRow(
          Html("Amount that was paid"),
          Html("£0.00"),
          rowClass = Some("govuk-summary-list__row--no-border"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-bottom-0"),
          valueClass = Some("govuk-!-padding-bottom-0")
        ),
        AnswerRow(
          Html("Amount that should have been paid"),
          Html("£0.00"),
          rowClass = Some("govuk-!-padding-top-0"),
          keyClass = Some("govuk-!-width-three-quarters govuk-!-padding-top-0"),
          valueClass = Some("govuk-!-padding-top-0")
        ),
        AnswerRow(
          Html("Total other duties return amount"),
          Html("<span class=\"govuk-!-font-weight-bold\">£0.00</span>"),
          Some("/apply-for-repayment-of-import-duty-and-import-vat/check-answers/change/otherDutiesPaid"),
          Some("other-duties-overpayment"),
          keyClass = Some("govuk-!-width-three-quarters")
        )
      )
    ),
    AnswerSection(
      Some("Total"),
      Seq(
        AnswerRow(
          Html("Total return amount"),
          Html("<span class=\"govuk-!-font-weight-bold\">£0.00</span>"),
          keyClass = Some("govuk-!-width-three-quarters")
        )
      )
    )
  )

  "RepaymentAmountSummary Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(
        userAnswersId,
        None,
        Json.obj(
          CustomsDutyPaidPage.toString -> Json.obj("ActualPaidAmount" -> "0.00", "ShouldHavePaidAmount" -> "0.00"),
          OtherDutiesPaidPage.toString -> Json.obj("ActualPaidAmount" -> "0.00", "ShouldHavePaidAmount" -> "0.00")
        )
      )
        .set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.RepaymentAmountSummaryController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RepaymentAmountSummaryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answersViewModel, defaultBackLink)(request, messages).toString

      application.stop()
    }
  }
}
