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

import controllers.actions._
import javax.inject.Inject
import models.{CheckMode, UserAnswers}
import pages.ClaimRepaymentTypePage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.RepaymentAmountSummaryAnswersHelper
import viewmodels.{AnswerRow, AnswerSection}
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RepaymentAmountSummaryView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val repaymentAmountSummaryAnswersHelper = new RepaymentAmountSummaryAnswersHelper(request.userAnswers)

      var customDutySection = AnswerSection(Some("Customs Duty"), Seq(
        displayCustomsDuty("0", request.userAnswers).get,
        displayCustomsDuty("1", request.userAnswers).get,
        displayCustomsDuty("2", request.userAnswers).get
      ))

      var vatSection = AnswerSection(Some("VAT"), Seq(
        displayVAT("0", request.userAnswers).get,
        displayVAT("1", request.userAnswers).get,
        displayVAT("2", request.userAnswers).get
      ))

      var otherDutiesSection = AnswerSection(Some("Other duties"), Seq(
        displayOtherDuties("0", request.userAnswers).get,
        displayOtherDuties("1", request.userAnswers).get,
        displayOtherDuties("2", request.userAnswers).get
      ))

      var totalSection = AnswerSection(Some("Total"), Seq(
        displayTotal(request.userAnswers).get
      ))

      val sections = Seq(Seq(customDutySection, vatSection, otherDutiesSection, totalSection
      )).flatten

      Ok(view(sections))
  }

  private def displayCustomsDuty(index: String, answers: UserAnswers)(implicit messages: Messages): Option[AnswerRow] = answers.get(ClaimRepaymentTypePage).map {
      x =>
          AnswerRow(
            Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.customsduty.$index")).toString).mkString("")),
            Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.cd.$index")).toString).mkString("")),
            routes.ClaimRepaymentTypeController.onPageLoad(CheckMode).url
          )
  }

  private def displayVAT(index: String, answers: UserAnswers)(implicit messages: Messages): Option[AnswerRow] = answers.get(ClaimRepaymentTypePage).map {
    x =>
      AnswerRow(
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.vat.$index")).toString).mkString("")),
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.cd.$index")).toString).mkString("")),
        routes.ClaimRepaymentTypeController.onPageLoad(CheckMode).url
      )
  }

  private def displayOtherDuties(index: String, answers: UserAnswers)(implicit messages: Messages): Option[AnswerRow] = answers.get(ClaimRepaymentTypePage).map {
    x =>
      AnswerRow(
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.otherduties.$index")).toString).mkString("")),
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.cd.$index")).toString).mkString("")),
        routes.ClaimRepaymentTypeController.onPageLoad(CheckMode).url
      )
  }

  private def displayTotal(answers: UserAnswers)(implicit messages: Messages): Option[AnswerRow] = answers.get(ClaimRepaymentTypePage).map {
    x =>
      AnswerRow(
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.total.amount")).toString).mkString("")),
        Html(x.map(value => HtmlFormat.escape(messages(s"repaymentAmountSummary.cd.1")).toString).mkString("")),""
      )
  }

}
