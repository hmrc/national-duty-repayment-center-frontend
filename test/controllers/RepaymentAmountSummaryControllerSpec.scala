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
import models.{ClaimRepaymentType, NormalMode, UserAnswers}
import pages.{ClaimRepaymentTypePage, CustomsDutyDueToHMRCPage, CustomsDutyPaidPage, OtherDutiesDueToHMRCPage, OtherDutiesPaidPage, VATDueToHMRCPage, VATPaidPage}
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
    AnswerSection(Some("Customs Duty"), Seq(
      AnswerRow(Html(
        "Customs Duty that was paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/change-customs-duty-paid"),
        Some("customs-duty-paid")
      ),
      AnswerRow(
        Html("Customs Duty that should have been paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/changeCustomsDutyDueToHMRC"),
        Some("customs-duty-due")
      ),
      AnswerRow(Html("Total Customs Duty repayment amount"), Html("<span class=\"bold\">£0.00</span>"))
    )),
    AnswerSection(Some("Import VAT"), Seq(
      AnswerRow(
        Html("Import VAT that was paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/change-import-vat-paid"),
        Some("vat-paid")
      ),
      AnswerRow(
        Html("Import VAT that should have been paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/changeVATDueToHMRC"),
        Some("vat-due")
      ),
      AnswerRow(Html("Total import VAT repayment amount"), Html("<span class=\"bold\">£0.00</span>"))
    )),
    AnswerSection(Some("Other duties"), Seq(
      AnswerRow(
        Html("Other duties that was paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/change-other-duties-paid"),
        Some("other-duties-paid")
      ),
      AnswerRow(
        Html("Other duties that should have been paid"),
        Html("£0.00"),
        Some("/national-duty-repayment-center/changeOtherDutiesDueToHMRC"),
        Some("other-duties-due")
      ),
      AnswerRow(Html("Total other duties repayment amount"), Html("<span class=\"bold\">£0.00</span>"))
    )),
    AnswerSection(Some("Total"), Seq(
      AnswerRow(Html("Total repayment amount"), Html("<span class=\"bold\">£0.00</span>"))
    ))
  )

  "RepaymentAmountSummary Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId).set(ClaimRepaymentTypePage, ClaimRepaymentType.values.toSet).
        success.value.set(CustomsDutyPaidPage, "0.00").success.value
        .set(CustomsDutyDueToHMRCPage, "0.00").success.value
        .set(VATPaidPage, "0.00").success.value
        .set(VATDueToHMRCPage, "0.00").success.value
        .set(OtherDutiesPaidPage, "0.00").success.value
        .set(OtherDutiesDueToHMRCPage, "0.00").success.value

      val backLink = userAnswers.get(ClaimRepaymentTypePage) match {
        case _ if userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Other) => routes.OtherDutiesDueToHMRCController.onPageLoad(NormalMode)
        case _ if userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Vat) => routes.VATDueToHMRCController.onPageLoad(NormalMode)
        case _ if userAnswers.get(ClaimRepaymentTypePage).get.contains(ClaimRepaymentType.Customs) => routes.CustomsDutyDueToHMRCController.onPageLoad(NormalMode)
        case _ => routes.ClaimRepaymentTypeController.onPageLoad(NormalMode)
      }

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.RepaymentAmountSummaryController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RepaymentAmountSummaryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
      view(answersViewModel, backLink)(fakeRequest, messages).toString

      application.stop()
    }
  }
}
