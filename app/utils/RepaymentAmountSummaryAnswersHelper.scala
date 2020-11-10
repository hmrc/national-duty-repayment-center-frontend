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

package utils

import controllers.routes
import models.{CheckMode, ClaimRepaymentType, UserAnswers}
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.{AnswerRow, AnswerSection}

class RepaymentAmountSummaryAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def displayDuty(index: String, amount: String, dutyType: String) : Option[AnswerRow] = {
    getAnswerRow(index, amount, dutyType)
  }

  private def getAnswerRow(index: String, amount: String, dutyType: String) : Option[AnswerRow] =
    userAnswers.get(ClaimRepaymentTypePage).map {
    val isCustomDutyExists = dutyType.equals("repaymentAmountSummary.customsduty")
    val isVATExists = dutyType.equals("repaymentAmountSummary.vat")
    val isOtherDutiesExists = dutyType.equals("repaymentAmountSummary.otherduties")

    val message : String = dutyType match {
      case y if isCustomDutyExists => messages(s"$dutyType.$index")
      case y if isVATExists => messages(s"$dutyType.$index")
      case y if isOtherDutiesExists => messages(s"$dutyType.$index")
      case _ => messages(s"$dutyType")
    }

    x =>
      AnswerRow(
        Html(x.map(value => HtmlFormat.escape(message).toString).mkString("")),
        Html(x.map(value => HtmlFormat.escape("£" + amount).toString).mkString("")),
        index match {
          case "0" if isCustomDutyExists => Some(routes.customsDutyPaidController.onPageLoad(CheckMode).url)
          case "1" if isCustomDutyExists => Some(routes.CustomsDutyDueToHMRCController.onPageLoad(CheckMode).url)
          case "0" if isVATExists => Some(routes.VATPaidController.onPageLoad(CheckMode).url)
          case "1" if isVATExists => Some(routes.VATDueToHMRCController.onPageLoad(CheckMode).url)
          case "0" if isOtherDutiesExists => Some(routes.OtherDutiesPaidController.onPageLoad(CheckMode).url)
          case "1" if isOtherDutiesExists => Some(routes.OtherDutiesDueToHMRCController.onPageLoad(CheckMode).url)
          case _ => None
        }
      )
  }

  def getSections(): Seq[AnswerSection] = {
    val claimRepaymentType: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get
    var sections = Seq[AnswerSection]()

    val customDutyPaid = userAnswers.get(customsDutyPaidPage).getOrElse("0")
    val customDutyDue = userAnswers.get(CustomsDutyDueToHMRCPage).getOrElse("0")
    val vatPaid = userAnswers.get(VATPaidPage).getOrElse("0")
    val vatDue = userAnswers.get(VATDueToHMRCPage).getOrElse("0")
    val otherDutiesPaid = userAnswers.get(OtherDutiesPaidPage).getOrElse("0")
    val otherDutiesDue = userAnswers.get(OtherDutiesDueToHMRCPage).getOrElse("0")

    for (claimType <- claimRepaymentType) {
      val selection: AnswerSection = claimRepaymentType match {
        case x if ClaimRepaymentType.Customs == claimType => getAnswerSection("repaymentAmountSummary.customsduty"
          , customDutyPaid, customDutyDue)
        case x if ClaimRepaymentType.Vat == claimType => getAnswerSection("repaymentAmountSummary.vat"
          , vatPaid, vatDue)
        case x if ClaimRepaymentType.Other == claimType => getAnswerSection("repaymentAmountSummary.otherduties"
          , otherDutiesPaid, otherDutiesDue)
      }

      sections = sections ++ Seq(selection)
    }

    if (claimRepaymentType.size > 1) {
      val totalSection = getAnswerSection("repaymentAmountSummary.total",
        (customDutyPaid.toInt - customDutyDue.toInt + vatPaid.toInt - vatDue.toInt +
        otherDutiesPaid.toInt - otherDutiesDue.toInt).toString,"")

      sections = sections ++ Seq(totalSection
      )
    }
    sections
  }

  private def getAnswerSection(dutyType: String, dutyPaid: String, dutyDue: String): AnswerSection = {
    val answerSection : AnswerSection = dutyType match {
      case "repaymentAmountSummary.total" =>
         AnswerSection(Some(dutyType), Seq(
          displayDuty("",dutyPaid, "repaymentAmountSummary.total.amount").get
        ))
      case _ =>
         AnswerSection(Some(dutyType), Seq(
          displayDuty("0", dutyPaid, dutyType).get,
          displayDuty("1", dutyDue, dutyType).get,
          displayDuty("2", (dutyPaid.toInt - dutyDue.toInt).toString, dutyType).get
        ))
    }
    answerSection
  }
}


