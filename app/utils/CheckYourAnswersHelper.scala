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

import java.time.format.DateTimeFormatter

import controllers.routes
import models.{AgentImporterHasEORI, ArticleType, CheckMode, ClaimantType, CustomsRegulationType, NumberOfEntriesType, RepaymentType, UserAnswers, WhomToPay}
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.{AnswerRow, AnswerSection}
import CheckYourAnswersHelper._
import models.FileType.{Bulk, SupportingEvidence}
import models.FileUpload.Accepted

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def amendCaseUploadAnotherFile: Option[AnswerRow] = userAnswers.get(AmendCaseUploadAnotherFilePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("amendCaseUploadAnotherFile.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"amendCaseUploadAnotherFile.$x")),
        Some(routes.AmendCaseSendInformationController.showFileUploaded(CheckMode).url)
      )
  }

  def amendCaseResponseType: Option[AnswerRow] = userAnswers.get(AmendCaseResponseTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("amendCaseResponseType.checkYourAnswersLabel")),
        Html(x.map(value => HtmlFormat.escape(messages(s"amendCaseResponseType.$value")).toString).mkString(",<br>")),
        Some(routes.AmendCaseResponseTypeController.onPageLoad(CheckMode).url)
      )
  }

  def furtherInformation: Option[AnswerRow] = userAnswers.get(FurtherInformationPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("furtherInformation.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.FurtherInformationController.onPageLoad(CheckMode).url)
      )
  }

  def referenceNumber: Option[AnswerRow] = userAnswers.get(ReferenceNumberPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("referenceNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.ReferenceNumberController.onPageLoad(CheckMode).url)
      )
  }

  def bulkFileUpload: Option[AnswerRow] = {
    userAnswers.fileUploadState.map(_.fileUploads.files.filter(_.fileType.contains(Bulk))).flatMap { f =>
      f.headOption.map { f =>
        f match {
          case Accepted(_, _, _, _, _, fileName, _, _) =>
            AnswerRow(
              HtmlFormat.escape(messages("bulkFileUpload.checkYourAnswersLabel")),
              HtmlFormat.escape(messages(s"$fileName")),
              Some(routes.BulkFileUploadController.showFileUpload.url)
            )
          case _ =>  AnswerRow(
            HtmlFormat.escape(messages("bulkFileUpload.checkYourAnswersLabel")),
            HtmlFormat.escape(messages(s"bulkFileUpload.empty")),
            Some(routes.BulkFileUploadController.showFileUpload.url)
          )
        }
      }
    }
  }

  def indirectRepresentative: Option[AnswerRow] = userAnswers.get(IndirectRepresentativePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("indirectRepresentative.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.IndirectRepresentativeController.onPageLoad(CheckMode).url)
      )
  }

  def bankDetails: Option[AnswerRow] = userAnswers.get(BankDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("bankDetails.checkYourAnswersLabel")),
        HtmlFormat.escape(x.AccountName.concat("\n").
          concat(x.SortCode).concat("\n").concat(x.AccountNumber)),
        Some(routes.BankDetailsController.onPageLoad(CheckMode).url)
      )
  }

  def agentImporterManualAddress: Option[AnswerRow] = userAnswers.get(AgentImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterManualAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.AddressLine1.concat("\n").
          concat(x.AddressLine2.getOrElse("")).concat("\n").
          concat(x.City).concat("\n").concat(x.Region.getOrElse("").concat("\n").
          concat(x.CountryCode).concat("\n").concat(x.PostalCode.getOrElse("")))),
        Some(routes.AgentImporterManualAddressController.onPageLoad(CheckMode).url)
      )
  }

  def importerManualAddress: Option[AnswerRow] = userAnswers.get(ImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerManualAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.toString),
        Some(routes.ImporterManualAddressController.onPageLoad(CheckMode).url)
      )
  }

  def agentImporterAddress: Option[AnswerRow] = userAnswers.get(AgentImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.AddressLine1.concat("\n").
          concat(x.AddressLine2.getOrElse("")).concat("\n").
          concat(x.City).concat("\n").concat(x.Region.getOrElse("").concat("\n").
          concat(x.CountryCode).concat("\n").concat(x.PostalCode.getOrElse("")))),
        Some(routes.AgentImporterAddressController.onPageLoad(CheckMode).url)
      )
  }

  def importerAddress: Option[AnswerRow] = userAnswers.get(ImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.AddressLine1.concat("\n").
          concat(x.AddressLine2.getOrElse("")).concat("\n").
          concat(x.City).concat("\n").concat(x.Region.getOrElse("").concat("\n").
          concat(x.CountryCode).concat("\n").concat(x.PostalCode.getOrElse("")))),
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
        HtmlFormat.escape(x.firstName.concat(x.lastName)),
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

  def contactByEmail: Option[AnswerRow] = userAnswers.get(EmailAddressPage) map {
    x => {
      AnswerRow(
        HtmlFormat.escape(messages("contactByEmail.checkYourAnswersLabel")),
        HtmlFormat.escape(x.isBlank match
              { case true => "No"
                case _ => "Yes"}),
        Some(routes.EmailAddressController.onPageLoad(CheckMode).url)
      )
    }
  }

  def contactByEmail: Option[AnswerRow] = userAnswers.get(EmailAddressPage) map {
    x => {
      AnswerRow(
        HtmlFormat.escape(messages("contactByEmail.checkYourAnswersLabel")),
        HtmlFormat.escape(x match
              { case x if x.length > 0 => "Yes"
                case _ => "No"}),
        Some(routes.EmailAddressController.onPageLoad(CheckMode).url)
      )
    }
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
        HtmlFormat.escape(x.firstName.concat(",").concat(x.lastName)),
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

  def repaymentAmountSummary: AnswerRow = {
    val helper = new RepaymentAmountSummaryAnswersHelper(userAnswers)
    AnswerRow(
    HtmlFormat.escape(messages("repaymentAmountSummary.total.checkYourAnswersLabel")),
    HtmlFormat.escape(helper.getTotalAmount.toString),
    Some(routes.RepaymentAmountSummaryController.onPageLoad.url)
    )
  }

  def evidenceFileUploads: AnswerRow = {
    AnswerRow(
      HtmlFormat.escape(messages("view.upload-file.checkYourAnswersLabel")),
      HtmlFormat.escape((userAnswers.fileUploadState.get.fileUploads.files.size.toString)
      .concat(" ").concat(messages("view.upload-file.documents.added"))),
      Some(routes.FileUploadController.showFileUploaded.url)
    )
  }

  def getImportantInformationAnswerSection: AnswerSection = {
    userAnswers.get(NumberOfEntriesTypePage) match {
      case Some(NumberOfEntriesType.Multiple) =>
        userAnswers.get(CustomsRegulationTypePage) match {
          case Some(CustomsRegulationType.UnionsCustomsCodeRegulation) =>
            AnswerSection (Some (messages ("impInfo.checkYourAnswersLabel") ),
            Seq (claimantType.get,
            numberOfEntriesType.get,
            howManyEntries.get,
            customsRegulationType.get,
            articleType.get))
          case _ =>
            AnswerSection (Some (messages ("impInfo.checkYourAnswersLabel") ),
            Seq (claimantType.get,
              numberOfEntriesType.get,
              howManyEntries.get,
              customsRegulationType.get))
        }
      case _ =>
        userAnswers.get(CustomsRegulationTypePage) match {
          case Some(CustomsRegulationType.UnionsCustomsCodeRegulation) =>
            AnswerSection(Some(messages("impInfo.checkYourAnswersLabel")),
              Seq(claimantType.get,
                numberOfEntriesType.get,
                customsRegulationType.get,
                articleType.get))
          case _ =>
            AnswerSection(Some(messages("impInfo.checkYourAnswersLabel")),
              Seq(claimantType.get,
                numberOfEntriesType.get,
                customsRegulationType.get))
        }
    }
  }

  def getEntryDetailsAnswerSection: AnswerSection = {
    userAnswers.get(NumberOfEntriesTypePage) match {
      case Some(NumberOfEntriesType.Multiple) =>
        AnswerSection(Some(messages("entryDetails.checkYourAnswersLabel")),
          Seq(bulkFileUpload.get,
            entryDetailsEPU.get,
            entryDetailsNumber.get,
            entryDetailsDate.get))
      case _ =>
        AnswerSection(Some(messages("entryDetails.checkYourAnswersLabel")),
          Seq(entryDetailsEPU.get,
            entryDetailsNumber.get,
            entryDetailsDate.get))
    }
  }

  def getApplicationInformationAnswerSection: AnswerSection = {
    AnswerSection(Some(messages("applicationInformation.checkYourAnswersLabel")),
      Seq(claimReasonType.get,
        reasonForOverpayment.get,
        claimRepaymentType.get,
        repaymentAmountSummary,
        evidenceFileUploads))
  }

  def getImporterDetailsAnswerSection: AnswerSection = {
    userAnswers.get(AgentImporterHasEORIPage) match {
      case Some(AgentImporterHasEORI.Yes) =>
        AnswerSection(Some(messages("importer.details.checkYourAnswersLabel")),
          Seq(agentImporterHasEORI.get,
            enterAgentEORI.get,
            isImporterVatRegistered.get,
            agentNameImporter.get,
            userAnswers.get(ImporterManualAddressPage) match {
              case None => importerAddress.get
              case _ => importerManualAddress.get
            }))
      case _ =>
        AnswerSection(Some(messages("importer.details.checkYourAnswersLabel")),
          Seq(agentImporterHasEORI.get,
            isImporterVatRegistered.get,
            agentNameImporter.get,
            userAnswers.get(ImporterManualAddressPage) match {
              case None => importerAddress.get
              case _ => importerManualAddress.get
            }))
    }
  }

  def getYourDetailsAnswerSection: AnswerSection = {
    userAnswers.get(ImporterHasEoriPage) match {
      case Some(true) =>
        AnswerSection(Some(messages("your.details.checkYourAnswersLabel")),
          Seq(importerHasEori.get,
            importerEori.get,
            userAnswers.get(ClaimantTypePage) match {
              case Some(ClaimantType.Importer) => isVATRegistered.get
              case _ => isImporterVatRegistered.get
            },
            importerName.get,
            userAnswers.get(ImporterManualAddressPage) match {
              case None => importerAddress.get
              case _ => importerManualAddress.get
            }))
      case _ =>
        AnswerSection(Some(messages("your.details.checkYourAnswersLabel")),
          Seq(importerHasEori.get,
            userAnswers.get(ClaimantTypePage) match {
              case Some(ClaimantType.Importer) => isVATRegistered.get
              case _ => isImporterVatRegistered.get
            },
            importerName.get,
            userAnswers.get(ImporterManualAddressPage) match {
              case None => importerAddress.get
              case _ => importerManualAddress.get
            }))
    }
  }

  def getContactDetailsAnswerSection: AnswerSection = {
    userAnswers.get(EmailAddressPage) match {
      case Some(email) if email.length > 0 =>
        AnswerSection (Some (messages ("contact.details.checkYourAnswersLabel") ),
        Seq (phoneNumber.get,
        contactByEmail.get,
        emailAddress.get))
      case _ =>
        AnswerSection (Some (messages ("contact.details.checkYourAnswersLabel") ),
          Seq (phoneNumber.get,
            contactByEmail.get) )
    }
  }

  def getPaymentInformationAnswerSection: AnswerSection = {
    userAnswers.get(RepaymentTypePage) match {
      case None =>
        AnswerSection (Some (messages ("payment.information.checkYourAnswersLabel") ),
          Seq (bankDetails.get))
      case _ =>
        AnswerSection (Some (messages ("payment.information.checkYourAnswersLabel") ),
        Seq (repaymentType.get, bankDetails.get) )
    }
  }

  private def entryDetailsEPU: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.epu.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EPU)),
        Some(routes.EntryDetailsController.onPageLoad(CheckMode).url)
      )
  }

  private def entryDetailsNumber: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.number.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EntryNumber)),
        Some(routes.EntryDetailsController.onPageLoad(CheckMode).url)
      )
  }

  private def entryDetailsDate: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.date.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EntryDate.toString)),
        Some(routes.EntryDetailsController.onPageLoad(CheckMode).url)
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
        HtmlFormat.escape(userAnswers.get(NumberOfEntriesTypePage) match {
          case Some(NumberOfEntriesType.Single) => messages("numberOfEntriesType.single.checkYourAnswersLabel")
          case _ => messages("numberOfEntriesType.multiple.checkYourAnswersLabel")
        }),
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

  def isVATRegistered: Option[AnswerRow] = userAnswers.get(IsVATRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isVATRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isVATRegistered.$x")),
        Some(routes.IsVATRegisteredController.onPageLoad(CheckMode).url)
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
        HtmlFormat.escape(userAnswers.get(ClaimantTypePage) match {
          case Some(ClaimantType.Importer) => messages("claimantType.importer")
          case _ => messages("claimantType.representative")
        }),
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
