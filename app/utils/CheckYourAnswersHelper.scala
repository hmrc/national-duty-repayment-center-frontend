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
import models.{Address, AgentImporterHasEORI, ClaimantType, CustomsRegulationType, NormalMode, NumberOfEntriesType, RepaymentType, UserAnswers, WhomToPay}
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.{AnswerRow, AnswerSection}
import models.FileType.{Bulk, ProofOfAuthority, SupportingEvidence}
import models.FileUpload.Accepted

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def amendCaseUploadAnotherFile: Option[AnswerRow] = userAnswers.get(AmendCaseUploadAnotherFilePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("amendCaseUploadAnotherFile.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"amendCaseUploadAnotherFile.$x")),
        Some(routes.AmendCaseSendInformationController.showFileUploaded(NormalMode).url)
      )
  }

  def amendCaseResponseType: Option[AnswerRow] = userAnswers.get(AmendCaseResponseTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("amendCaseResponseType.checkYourAnswersLabel")),
        Html(x.map(value => HtmlFormat.escape(messages(s"amendCaseResponseType.$value")).toString).mkString(",<br>")),
        Some(routes.AmendCaseResponseTypeController.onPageLoad(NormalMode).url)
      )
  }

  def furtherInformation: Option[AnswerRow] = userAnswers.get(FurtherInformationPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("furtherInformation.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.FurtherInformationController.onPageLoad(NormalMode).url)
      )
  }

  def referenceNumber: Option[AnswerRow] = userAnswers.get(ReferenceNumberPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("referenceNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.ReferenceNumberController.onPageLoad(NormalMode).url)
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

  def proofOfAuthority: Option[AnswerRow] = {
    userAnswers.fileUploadState.map(_.fileUploads.files.filter(_.fileType.contains(ProofOfAuthority))).flatMap { f =>
      f.headOption.map { f =>
        f match {
          case Accepted(_, _, _, _, _, fileName, _, _) =>
            AnswerRow(
              HtmlFormat.escape(messages("proofOfAuthority.checkYourAnswersLabel")),
              HtmlFormat.escape(messages(s"$fileName")),
              Some(routes.ProofOfAuthorityController.showFileUpload.url)
            )
          case _ =>  AnswerRow(
            HtmlFormat.escape(messages("proofOfAuthority.checkYourAnswersLabel")),
            HtmlFormat.escape(messages(s"proofOfAuthority.empty")),
            Some(routes.ProofOfAuthorityController.showFileUpload.url)
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
        Some(routes.IndirectRepresentativeController.onPageLoad(NormalMode).url)
      )
  }

  def bankDetails: Option[AnswerRow] = userAnswers.get(BankDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("bankDetails.checkYourAnswersLabel")),
        Html(Seq(
          HtmlFormat.escape(x.AccountName).toString,
          HtmlFormat.escape(x.SortCode).toString,
          HtmlFormat.escape(x.AccountNumber).toString
        ).mkString("<br>")),
        Some(routes.BankDetailsController.onPageLoad(NormalMode).url)
      )
  }

  def agentImporterManualAddress: Option[AnswerRow] = userAnswers.get(AgentImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterManualAddress.checkYourAnswersLabel")),
        formatAddress(x),
        Some(routes.AgentImporterManualAddressController.onPageLoad(NormalMode).url)
      )
  }

  def importerManualAddress: Option[AnswerRow] = userAnswers.get(ImporterManualAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(
          userAnswers.get(ClaimantTypePage) match {
            case Some(ClaimantType.Importer) => messages("agentImporterAddress.checkYourAnswersLabel")
            case _ => messages("importerAddress.checkYourAnswersLabel")
          }),
        formatAddress(x),
        Some(routes.ImporterManualAddressController.onPageLoad(NormalMode).url)
      )
  }

  def agentImporterAddress: Option[AnswerRow] = userAnswers.get(AgentImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterAddress.checkYourAnswersLabel")),
        formatAddress(x),
        Some(routes.AgentImporterAddressController.onPageLoad(NormalMode).url)
      )
  }

  def importerAddress: Option[AnswerRow] = userAnswers.get(ImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(
          userAnswers.get(ClaimantTypePage) match {
            case Some(ClaimantType.Importer) => messages("agentImporterAddress.checkYourAnswersLabel")
            case _ => messages("importerAddress.checkYourAnswersLabel")
          }),
        formatAddress(x),
        Some(routes.ImporterAddressController.onPageLoad(NormalMode).url)
      )
  }

  private def formatAddress(x: Address) = {
    Html(Seq(
      HtmlFormat.escape(x.AddressLine1).toString,
      HtmlFormat.escape(x.AddressLine2.getOrElse("")).toString,
      HtmlFormat.escape(x.City).toString,
      HtmlFormat.escape(x.Region.getOrElse("")).toString,
      HtmlFormat.escape(x.CountryCode match
            {case "GB" => messages("United Kingdom")
             case _ => messages("Other")
      }).toString,
      HtmlFormat.escape(x.PostalCode.getOrElse("")).toString
    ).filter(!_.isEmpty()).mkString("<br>"))
  }

  def otherDutiesDueToHMRC: Option[AnswerRow] = userAnswers.get(OtherDutiesDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("otherDutiesDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.OtherDutiesDueToHMRCController.onPageLoad(NormalMode).url)
      )
  }

  def otherDutiesPaid: Option[AnswerRow] = userAnswers.get(OtherDutiesPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("otherDutiesPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.OtherDutiesPaidController.onPageLoad(NormalMode).url)
      )
  }

  def customsDutyDueToHMRC: Option[AnswerRow] = userAnswers.get(CustomsDutyDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsDutyDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.CustomsDutyDueToHMRCController.onPageLoad(NormalMode).url)
      )
  }

  def customsDutyPaid: Option[AnswerRow] = userAnswers.get(CustomsDutyPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsDutyPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.CustomsDutyPaidController.onPageLoad(NormalMode).url)
      )
  }

  def vATDueToHMRC: Option[AnswerRow] = userAnswers.get(VATDueToHMRCPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("vATDueToHMRC.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.VATDueToHMRCController.onPageLoad(NormalMode).url)
      )
  }

  def vATPaid: Option[AnswerRow] = userAnswers.get(VATPaidPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("vATPaid.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.VATPaidController.onPageLoad(NormalMode).url)
      )
  }

  def agentImporterHasEORI: Option[AnswerRow] = userAnswers.get(AgentImporterHasEORIPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterHasEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"agentImporterHasEORI.$x")),
        Some(routes.AgentImporterHasEORIController.onPageLoad(NormalMode).url)
    )
  }
  
  def isImporterVatRegistered: Option[AnswerRow] = userAnswers.get(IsImporterVatRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isImporterVatRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isImporterVatRegistered.$x")),
        Some(routes.IsImporterVatRegisteredController.onPageLoad(NormalMode).url)
      )
  }

  def enterAgentEORI: Option[AnswerRow] = userAnswers.get(EnterAgentEORIPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("enterAgentEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.EnterAgentEORIController.onPageLoad(NormalMode).url)
      )
  }


  def whomToPay: Option[AnswerRow] = userAnswers.get(WhomToPayPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("whomToPay.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"whomToPay.$x")),
        Some(routes.WhomToPayController.onPageLoad(NormalMode).url)
      )
  }

  def repaymentType: Option[AnswerRow] = userAnswers.get(RepaymentTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("repaymentType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"repaymentType.$x")),
        Some(routes.RepaymentTypeController.onPageLoad(NormalMode).url)
      )
  }


  def agentNameImporter: Option[AnswerRow] = userAnswers.get(AgentNameImporterPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentNameImporter.checkYourAnswersLabel")),
        HtmlFormat.escape(x.firstName.concat(" ").concat(x.lastName)),
        Some(routes.AgentNameImporterController.onPageLoad(NormalMode).url)
      )
  }

  def phoneNumber: Option[AnswerRow] = userAnswers.get(PhoneNumberPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("phoneNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.PhoneNumberController.onPageLoad(NormalMode).url)
      )
  }

  def emailAddress: Option[AnswerRow] = userAnswers.get(EmailAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("emailAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.EmailAddressController.onPageLoad(NormalMode).url)
      )
  }

  def contactByEmail: Option[AnswerRow] = userAnswers.get(EmailAddressPage) map {
    x => {
      AnswerRow(
        HtmlFormat.escape(messages("contactByEmail.checkYourAnswersLabel")),
        HtmlFormat.escape(x match
              { case x if x.length > 0 => "Yes"
                case _ => "No"}),
        Some(routes.EmailAddressController.onPageLoad(NormalMode).url)
      )
    }
  }

  def contactType: Option[AnswerRow] = userAnswers.get(ContactTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("contactType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"contactType.$x")),
        Some(routes.ContactTypeController.onPageLoad(NormalMode).url)
      )
  }

  def importerName: Option[AnswerRow] = userAnswers.get(ImporterNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerName.checkYourAnswersLabel")),
        HtmlFormat.escape(x.firstName.concat(" ").concat(x.lastName)),
        Some(routes.ImporterNameController.onPageLoad(NormalMode).url)
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
        Some(routes.ClaimRepaymentTypeController.onPageLoad(NormalMode).url)
      )
  }

  def reasonForOverpayment: Option[AnswerRow] = userAnswers.get(ReasonForOverpaymentPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("reasonForOverpayment.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ReasonForOverpaymentController.onPageLoad(NormalMode).url)
      )
  }

  def claimReasonType: Option[AnswerRow] = userAnswers.get(ClaimReasonTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimReasonType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"claimReasonType.$x")),
        Some(routes.ClaimReasonTypeController.onPageLoad(NormalMode).url)
      )
  }

  def repaymentAmountSummary: AnswerRow = {
    val helper = new RepaymentAmountSummaryAnswersHelper(userAnswers)
    AnswerRow(
    HtmlFormat.escape(messages("repaymentAmountSummary.total.checkYourAnswersLabel")),
      HtmlFormat.escape("£" + helper.getTotalAmount().format2d),
    Some(routes.RepaymentAmountSummaryController.onPageLoad.url)
    )
  }

  def evidenceFileUploads: AnswerRow = {
    AnswerRow(
      HtmlFormat.escape(messages("view.upload-file.checkYourAnswersLabel")),
      HtmlFormat.escape((userAnswers.fileUploadState.get.fileUploads.acceptedCount.toString)
      .concat(" ").concat(messages("view.upload-file.documents.added"))),
      Some(routes.FileUploadController.showFileUploaded.url)
    )
  }

  def getCheckYourAnswerSections: Seq[AnswerSection] = {
    Seq(getImportantInformationAnswerSection,
      getEntryDetailsAnswerSection,
      getApplicationInformationAnswerSection) ++
      (userAnswers.get(ClaimantTypePage).contains(ClaimantType.Representative) match {
        case true => Seq(getImporterDetailsAnswerSection)
        case _ => Seq.empty
      }) ++
      Seq(getYourDetailsAnswerSection,
        getContactDetailsAnswerSection,
        getPaymentInformationAnswerSection)
  }

  def getImportantInformationAnswerSection: AnswerSection = {
    AnswerSection (Some(messages ("impInfo.checkYourAnswersLabel") ),
      Seq(claimantType.get,
        numberOfEntriesType.get) ++
        (userAnswers.get(NumberOfEntriesTypePage) match {
            case Some(NumberOfEntriesType.Multiple) => Seq (howManyEntries.get)
            case _ => Seq.empty
        }) ++
        Seq(customsRegulationType.get) ++
        (userAnswers.get(CustomsRegulationTypePage) match {
            case Some(CustomsRegulationType.UnionsCustomsCodeRegulation) => Seq(articleType.get)
            case _ => Seq(ukRegulationType.get)
        })
    )
  }

  def getEntryDetailsAnswerSection: AnswerSection = {
    AnswerSection(Some(messages("entryDetails.checkYourAnswersLabel")),
      Seq.empty ++
      (userAnswers.get(NumberOfEntriesTypePage) match {
      case Some(NumberOfEntriesType.Multiple) => Seq(bulkFileUpload.get)
      case _ => Seq.empty
      }) ++
      Seq(entryDetailsEPU.get,
        entryDetailsNumber.get,
        entryDetailsDate.get)
    )
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
    AnswerSection(Some(messages("importer.details.checkYourAnswersLabel")),
      Seq(agentImporterHasEORI.get) ++
        (userAnswers.get(AgentImporterHasEORIPage) match {
            case Some(AgentImporterHasEORI.Yes) => Seq(enterAgentEORI.get)
            case _  => Seq.empty
        }) ++
        Seq(
          isImporterVatRegistered.get,
          agentNameImporter.get,
          userAnswers.get(ImporterManualAddressPage) match {
            case None => importerAddress.get
            case _ => importerManualAddress.get
          })
    )
  }

  def getYourDetailsAnswerSection: AnswerSection = {
    AnswerSection(Some(messages("your.details.checkYourAnswersLabel")),
      Seq(importerHasEori.get) ++
        (userAnswers.get(ImporterHasEoriPage).get match {
          case true => Seq(importerEori.get)
          case _ => Seq.empty
        }) ++
        (userAnswers.get(ClaimantTypePage).contains(ClaimantType.Importer) match {
          case true => Seq(isVATRegistered.get)
          case _ => Seq.empty}
          ) ++
        Seq(importerName.get,
          userAnswers.get(ClaimantTypePage) match {
            case Some(ClaimantType.Importer) =>
                userAnswers.get(ImporterManualAddressPage) match {
                  case None => importerAddress.get
                  case _ => importerManualAddress.get
                }
            case _ =>
              userAnswers.get(AgentImporterManualAddressPage) match {
                case None => agentImporterAddress.get
                case _ => agentImporterManualAddress.get
              }
          }))
  }

  def getContactDetailsAnswerSection: AnswerSection = {
    AnswerSection (Some (messages ("contact.details.checkYourAnswersLabel") ),
      Seq (phoneNumber.get,
        contactByEmail.get) ++
        (userAnswers.get(EmailAddressPage).get.isEmpty match {
          case true => Seq.empty
          case _ => Seq(emailAddress.get)
        })
    )
  }

  def getPaymentInformationAnswerSection: AnswerSection = {
    AnswerSection (Some (messages ("payment.information.checkYourAnswersLabel") ),
      Seq.empty ++
        (userAnswers.get(RepaymentTypePage).contains(RepaymentType.BACS) match {
          case true =>
            (userAnswers.get(ClaimantTypePage).contains(ClaimantType.Representative) match {
              case true => (userAnswers.get(WhomToPayPage) match {
                case None => Seq.empty
                case _ => Seq(whomToPay.get)
              })
              case _ => Seq.empty
            }) ++
              (userAnswers.get(ClaimantTypePage).contains(ClaimantType.Representative) match {
                case true => (userAnswers.get(WhomToPayPage).contains(WhomToPay.Representative) match {
                  case true => Seq(indirectRepresentative.get)
                  case _ => Seq.empty
                })
                case _ => Seq.empty
              }) ++
              (userAnswers.get(ClaimantTypePage).contains(ClaimantType.Representative) match {
                case true => ((userAnswers.get(IndirectRepresentativePage),userAnswers.get(WhomToPayPage)) match {
                  case (Some(false), Some(WhomToPay.Representative))=> Seq(proofOfAuthority.get)
                  case _ => Seq.empty
                })
                case _ => Seq.empty
              })
          case _ => Seq.empty
        }) ++
        (userAnswers.get(RepaymentTypePage) match {
          case None => Seq.empty
          case _ => (userAnswers.get(NumberOfEntriesTypePage) match {
            case Some(NumberOfEntriesType.Single) => Seq(repaymentType.get)
            case _ => Seq.empty
          })
        }) ++
        (userAnswers.get(RepaymentTypePage).contains(RepaymentType.CMA) match {
          case true => Seq.empty
          case _ => Seq(bankDetails.get)
        })
    )
  }

  private def entryDetailsEPU: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.epu.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EPU)),
        Some(routes.EntryDetailsController.onPageLoad(NormalMode).url)
      )
  }

  private def entryDetailsNumber: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.number.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EntryNumber)),
        Some(routes.EntryDetailsController.onPageLoad(NormalMode).url)
      )
  }

  private def entryDetailsDate: Option[AnswerRow] = userAnswers.get(EntryDetailsPage) map {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.date.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EntryDate.format(dateFormatter))),
        Some(routes.EntryDetailsController.onPageLoad(NormalMode).url)
      )
  }

  def howManyEntries: Option[AnswerRow] = userAnswers.get(HowManyEntriesPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("howManyEntries.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.NumberOfEntriesTypeController.onPageLoad(NormalMode).url)
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
        Some(routes.NumberOfEntriesTypeController.onPageLoad(NormalMode).url)
      )
  }

  def articleType: Option[AnswerRow] = userAnswers.get(ArticleTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("articleType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"articleType.$x")),
        Some(routes.ArticleTypeController.onPageLoad(NormalMode).url)
      )
  }

  def ukRegulationType: Option[AnswerRow] = userAnswers.get(UkRegulationTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("ukRegulationType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"ukRegulationType.$x")),
        Some(routes.UkRegulationTypeController.onPageLoad(NormalMode).url)
      )
  }

  def customsRegulationType: Option[AnswerRow] = userAnswers.get(CustomsRegulationTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsRegulationType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"customsRegulationType.$x")),
        Some(routes.CustomsRegulationTypeController.onPageLoad(NormalMode).url)
      )
  }

  def isVATRegistered: Option[AnswerRow] = userAnswers.get(IsVATRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isVATRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isVATRegistered.$x")),
        Some(routes.IsVATRegisteredController.onPageLoad(NormalMode).url)
      )
  }

  def importerEori: Option[AnswerRow] = userAnswers.get(ImporterEoriPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerEori.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.ImporterEoriController.onPageLoad(NormalMode).url)
      )
  }

  def importerHasEori: Option[AnswerRow] = userAnswers.get(ImporterHasEoriPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerHasEori.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.ImporterHasEoriController.onPageLoad(NormalMode).url)
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
        Some(routes.ClaimantTypeController.onPageLoad(NormalMode).url)
      )
  }

  private def yesOrNo(answer: Boolean)(implicit messages: Messages): Html =
    if (answer) {
      HtmlFormat.escape(messages("site.yes"))
    } else {
      HtmlFormat.escape(messages("site.no"))
    }

  implicit class Improvements(s: Double) {
    def format2d = "%.2f".format(s)
  }
}

object CheckYourAnswersHelper {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
