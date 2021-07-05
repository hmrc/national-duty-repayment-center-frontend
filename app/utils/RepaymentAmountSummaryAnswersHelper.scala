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

package utils

import controllers.routes
import models.{ClaimRepaymentType, UserAnswers}
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.{AnswerRow, AnswerSection}

class RepaymentAmountSummaryAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  implicit class Improvements(s: Double) {
    def format2d = "%.2f".format(s)
  }

  def displayDuty(index: String, amount: Double, dutyType: String): AnswerRow =
    getAnswerRow(index, amount, dutyType).getOrElse(AnswerRow(Html(""), Html("")))

  private def getAnswerRow(index: String, amount: Double, dutyType: String): Option[AnswerRow] =
    userAnswers.get(ClaimRepaymentTypePage).map {

      val isCustomDutyExists  = dutyType.equals("repaymentAmountSummary.customsduty")
      val isVATExists         = dutyType.equals("repaymentAmountSummary.vat")
      val isOtherDutiesExists = dutyType.equals("repaymentAmountSummary.otherduties")

      val message: String = dutyType match {
        case y if isCustomDutyExists  => messages(s"$dutyType.$index")
        case y if isVATExists         => messages(s"$dutyType.$index")
        case y if isOtherDutiesExists => messages(s"$dutyType.$index")
        case _                        => messages(s"$dutyType")
      }

      def formattedAmount: String = index match {
        case "2" => "<span class=\"bold\">" + HtmlFormat.escape("£" + amount.format2d) + "</span>"
        case "0" if dutyType == "repaymentAmountSummary.total.amount" =>
          "<span class=\"bold\">" + HtmlFormat.escape("£" + amount.format2d) + "</span>"
        case _ => HtmlFormat.escape("£" + amount.format2d).toString()
      }

      x =>
        AnswerRow(
          Html(x.map(value => HtmlFormat.escape(message).toString).mkString("")),
          Html(x.map(value => formattedAmount).mkString("")),
          index match {
            case "0" if isCustomDutyExists =>
              Some(routes.CheckYourAnswersController.onChange(CustomsDutyPaidPage).url)
            case "1" if isCustomDutyExists =>
              Some(routes.CheckYourAnswersController.onChange(CustomsDutyPaidPage).url)
            case "0" if isVATExists =>
              Some(routes.CheckYourAnswersController.onChange(VATPaidPage).url)
            case "1" if isVATExists =>
              Some(routes.CheckYourAnswersController.onChange(VATPaidPage).url)
            case "0" if isOtherDutiesExists =>
              Some(routes.CheckYourAnswersController.onChange(OtherDutiesPaidPage).url)
            case "1" if isOtherDutiesExists =>
              Some(routes.CheckYourAnswersController.onChange(OtherDutiesPaidPage).url)
            case _ => None
          },
          index match {
            case "0" if isCustomDutyExists  => Some("customs-duty-overpayment")
            case "1" if isCustomDutyExists  => Some("customs-duty-overpayment")
            case "0" if isVATExists         => Some("change-import-vat-overpayment")
            case "1" if isVATExists         => Some("change-import-vat-overpayment")
            case "0" if isOtherDutiesExists => Some("other-duties-overpayment")
            case "1" if isOtherDutiesExists => Some("other-duties-overpayment")
            case _                          => None
          }
        )
    }

  def getSections(): Seq[AnswerSection] = {

    val claimRepaymentType: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).getOrElse(Set.empty)
    val customDutyPaid                              = userAnswers.get(CustomsDutyPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val customDutyDue                               = userAnswers.get(CustomsDutyPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble
    val vatPaid                                     = userAnswers.get(VATPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val vatDue                                      = userAnswers.get(VATPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble
    val otherDutiesPaid                             = userAnswers.get(OtherDutiesPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val otherDutiesDue                              = userAnswers.get(OtherDutiesPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble

    val sections: Seq[AnswerSection] = claimRepaymentType.map {
      case ClaimRepaymentType.Customs =>
        getAnswerSection("repaymentAmountSummary.customsduty", customDutyPaid, customDutyDue)
      case ClaimRepaymentType.Vat => getAnswerSection("repaymentAmountSummary.vat", vatPaid, vatDue)
      case ClaimRepaymentType.Other =>
        getAnswerSection("repaymentAmountSummary.otherduties", otherDutiesPaid, otherDutiesDue)
    }.toSeq

    sections
  }

  def getTotalAmount(): Double = {
    val customDutyPaid  = userAnswers.get(CustomsDutyPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val customDutyDue   = userAnswers.get(CustomsDutyPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble
    val vatPaid         = userAnswers.get(VATPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val vatDue          = userAnswers.get(VATPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble
    val otherDutiesPaid = userAnswers.get(OtherDutiesPaidPage).map(_.ActualPaidAmount).getOrElse("0.0").toDouble
    val otherDutiesDue  = userAnswers.get(OtherDutiesPaidPage).map(_.ShouldHavePaidAmount).getOrElse("0.0").toDouble

    (customDutyPaid - customDutyDue + vatPaid - vatDue +
      otherDutiesPaid - otherDutiesDue)
  }

  def getTotalSection(): AnswerSection =
    getAnswerSection("repaymentAmountSummary.total", getTotalAmount(), 0.0)

  private def getAnswerSection(dutyType: String, dutyPaid: Double, dutyDue: Double): AnswerSection = {
    val answerSection: AnswerSection = dutyType match {
      case "repaymentAmountSummary.total" =>
        AnswerSection(Some(dutyType), Seq(displayDuty("0", dutyPaid, "repaymentAmountSummary.total.amount")))
      case _ =>
        AnswerSection(
          Some(dutyType),
          Seq(
            displayDuty("0", dutyPaid, dutyType),
            displayDuty("1", dutyDue, dutyType),
            displayDuty("2", (dutyPaid - dutyDue), dutyType)
          )
        )
    }
    answerSection
  }

}
