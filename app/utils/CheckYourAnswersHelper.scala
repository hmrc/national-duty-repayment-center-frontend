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

import java.time.format.DateTimeFormatter

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.AnswerRow
import CheckYourAnswersHelper._

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def contactByEmail: Option[AnswerRow] = userAnswers.get(ContactByEmailPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("contactByEmail.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"contactByEmail.$x")),
        Some(routes.ContactByEmailController.onPageLoad(CheckMode).url)
      )
  }

  def additionalFileUpload: Option[AnswerRow] = userAnswers.get(AdditionalFileUploadPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("additionalFileUpload.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"additionalFileUpload.$x")),
        Some(routes.AdditionalFileUploadController.onPageLoad(CheckMode).url)
      )
  }

  def bankDetails: Option[AnswerRow] = userAnswers.get(BankDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("bankDetails.checkYourAnswersLabel")),
        HtmlFormat.escape(x.AccountName),
        Some(routes.BankDetailsController.onPageLoad(CheckMode).url)
      )
  }

  def agentImporterManualAddress: Option[AnswerRow] = userAnswers.get(AgentImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterManualAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.AgentImporterManualAddressController.onPageLoad(CheckMode).url)
      )
  }

  def importerManualAddress: Option[AnswerRow] = userAnswers.get(ImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerManualAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.ImporterManualAddressController.onPageLoad(CheckMode).url)
      )
  }

  def agentImporterAddress: Option[AnswerRow] = userAnswers.get(AgentImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.toString),
        Some(routes.AgentImporterAddressController.onPageLoad(CheckMode).url)
      )
  }

  def importerAddress: Option[AnswerRow] = userAnswers.get(ImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.toString),
        Some(routes.ImporterAddressController.onPageLoad(CheckMode).url)
      )
  }

  def otherDutiesDueToHMRC: Option[AnswerRow] = userAnswers.get(OtherDutiesDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("otherDutiesDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.OtherDutiesDueToHMRCController.onPageLoad(CheckMode).url)
      )
  }

  def otherDutiesPaid: Option[AnswerRow] = userAnswers.get(OtherDutiesPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("otherDutiesPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.OtherDutiesPaidController.onPageLoad(CheckMode).url)
      )
  }

  def customsDutyDueToHMRC: Option[AnswerRow] = userAnswers.get(CustomsDutyDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsDutyDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.CustomsDutyDueToHMRCController.onPageLoad(CheckMode).url)
      )
  }

  def customsDutyPaid: Option[AnswerRow] = userAnswers.get(CustomsDutyPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsDutyPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.CustomsDutyPaidController.onPageLoad(CheckMode).url)
      )
  }

  def vATDueToHMRC: Option[AnswerRow] = userAnswers.get(VATDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("vATDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.VATDueToHMRCController.onPageLoad(CheckMode).url)
      )
  }

  def vATPaid: Option[AnswerRow] = userAnswers.get(VATPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("vATPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.VATPaidController.onPageLoad(CheckMode).url)
      )
  }

  def agentImporterHasEORI: Option[AnswerRow] = userAnswers.get(AgentImporterHasEORIPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterHasEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"agentImporterHasEORI.$x")),
        Some(routes.AgentImporterHasEORIController.onPageLoad(CheckMode).url)
    )
  }
  
  def isImporterVatRegistered: Option[AnswerRow] = userAnswers.get(IsImporterVatRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isImporterVatRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isImporterVatRegistered.$x")),
        Some(routes.IsImporterVatRegisteredController.onPageLoad(CheckMode).url)
      )
  }

  def enterAgentEORI: Option[AnswerRow] = userAnswers.get(EnterAgentEORIPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("enterAgentEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.EnterAgentEORIController.onPageLoad(CheckMode).url)
      )
  }


  def whomToPay: Option[AnswerRow] = userAnswers.get(WhomToPayPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("whomToPay.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"whomToPay.$x")),
        Some(routes.WhomToPayController.onPageLoad(CheckMode).url)
      )
  }

  def repaymentType: Option[AnswerRow] = userAnswers.get(RepaymentTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("repaymentType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"repaymentType.$x")),
        Some(routes.RepaymentTypeController.onPageLoad(CheckMode).url)
      )
  }


  def agentNameImporter: Option[AnswerRow] = userAnswers.get(AgentNameImporterPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentNameImporter.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.AgentNameImporterController.onPageLoad(CheckMode).url)
      )
  }

  def phoneNumber: Option[AnswerRow] = userAnswers.get(PhoneNumberPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("phoneNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.PhoneNumberController.onPageLoad(CheckMode).url)
      )
  }

  def emailAddress: Option[AnswerRow] = userAnswers.get(EmailAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("emailAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.EmailAddressController.onPageLoad(CheckMode).url)
      )
  }

  def contactType: Option[AnswerRow] = userAnswers.get(ContactTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("contactType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"contactType.$x")),
        Some(routes.ContactTypeController.onPageLoad(CheckMode).url)
      )
  }

  def importerName: Option[AnswerRow] = userAnswers.get(ImporterNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerName.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ImporterNameController.onPageLoad(CheckMode).url)
      )
  }

  def evidenceSupportingDocs: Option[AnswerRow] = userAnswers.get(EvidenceSupportingDocsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("evidenceSupportingDocs.checkYourAnswersLabel")),
        Html(x.map(value => HtmlFormat.escape(messages(s"evidenceSupportingDocs.$value")).toString).mkString(",<br>")),
        Some(routes.EvidenceSupportingDocsController.onPageLoad().url)
      )
  }

  def claimRepaymentType: Option[AnswerRow] = userAnswers.get(ClaimRepaymentTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimRepaymentType.checkYourAnswersLabel")),
        Html(x.map(value => HtmlFormat.escape(messages(s"claimRepaymentType.$value")).toString).mkString(",<br>")),
        Some(routes.ClaimRepaymentTypeController.onPageLoad(CheckMode).url)
      )
  }

  def reasonForOverpayment: Option[AnswerRow] = userAnswers.get(ReasonForOverpaymentPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("reasonForOverpayment.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ReasonForOverpaymentController.onPageLoad(CheckMode).url)
      )
  }

  def whatAreTheGoods: Option[AnswerRow] = userAnswers.get(WhatAreTheGoodsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("whatAreTheGoods.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.WhatAreTheGoodsController.onPageLoad(CheckMode).url)
      )
  }

  def claimReasonType: Option[AnswerRow] = userAnswers.get(ClaimReasonTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimReasonType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"claimReasonType.$x")),
        Some(routes.ClaimReasonTypeController.onPageLoad(CheckMode).url)
      )
  }

  def claimEntryDate: Option[AnswerRow] = userAnswers.get(ClaimEntryDatePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimEntryDate.checkYourAnswersLabel")),
        HtmlFormat.escape(x.format(dateFormatter)),
        Some(routes.ClaimEntryDateController.onPageLoad(CheckMode).url)
      )
  }

  def claimEntryNumber: Option[AnswerRow] = userAnswers.get(ClaimEntryNumberPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimEntryNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ClaimEntryNumberController.onPageLoad(CheckMode).url)
      )
  }

  def claimEpu: Option[AnswerRow] = userAnswers.get(ClaimEpuPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimEpu.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ClaimEpuController.onPageLoad(CheckMode).url)
      )
  }

  def howManyEntries: Option[AnswerRow] = userAnswers.get(HowManyEntriesPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("howManyEntries.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.HowManyEntriesController.onPageLoad(CheckMode).url)
      )
  }

  def numberOfEntriesType: Option[AnswerRow] = userAnswers.get(NumberOfEntriesTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("numberOfEntriesType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"numberOfEntriesType.$x")),
        Some(routes.NumberOfEntriesTypeController.onPageLoad(CheckMode).url)
      )
  }

  def articleType: Option[AnswerRow] = userAnswers.get(ArticleTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("articleType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"articleType.$x")),
        Some(routes.ArticleTypeController.onPageLoad(CheckMode).url)
      )
  }

  def customsRegulationType: Option[AnswerRow] = userAnswers.get(CustomsRegulationTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsRegulationType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"customsRegulationType.$x")),
        Some(routes.CustomsRegulationTypeController.onPageLoad(CheckMode).url)
      )
  }

  def importerClaimantVrn: Option[AnswerRow] = userAnswers.get(ImporterClaimantVrnPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerClaimantVrn.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.ImporterClaimantVrnController.onPageLoad(CheckMode).url)
      )
  }

  def isVatRegistered: Option[AnswerRow] = userAnswers.get(IsVatRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isVatRegistered.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.IsVatRegisteredController.onPageLoad(CheckMode).url)
      )
  }

  def importerEori: Option[AnswerRow] = userAnswers.get(ImporterEoriPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerEori.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ImporterEoriController.onPageLoad(CheckMode).url)
      )
  }

  def importerHasEori: Option[AnswerRow] = userAnswers.get(ImporterHasEoriPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerHasEori.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.ImporterHasEoriController.onPageLoad(CheckMode).url)
      )
  }

  def claimantType: Option[AnswerRow] = userAnswers.get(ClaimantTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimantType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"claimantType.$x")),
        Some(routes.ClaimantTypeController.onPageLoad(CheckMode).url)
      )
  }

  private def yesOrNo(answer: Boolean)(implicit messages: Messages): Html =
    if (answer) {
      HtmlFormat.escape(messages("site.yes"))
    } else {
      HtmlFormat.escape(messages("site.no"))
    }
}

object CheckYourAnswersHelper {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
