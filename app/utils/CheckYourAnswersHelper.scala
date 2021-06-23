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
import models.AmendCaseResponseType.{FurtherInformation, SupportingDocuments}
import models.DeclarantReferenceType.{No, Yes}
import models.FileType.{Bulk, ProofOfAuthority}
import models.FileUpload.Accepted
import models.NumberOfEntriesType.{Multiple, Single}
import models._
import pages._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import queries.{ClaimDateQuery, ClaimIdQuery}
import viewmodels.{AnswerRow, AnswerSection}
import views.DateTimeFormats

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  // TODO - change format to GDS guidelines
  val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  val amendCaseResponseTypePage      = userAnswers.get(AmendCaseResponseTypePage)
  val furtherInformationPage         = userAnswers.get(FurtherInformationPage)
  val referenceNumberPage            = userAnswers.get(ReferenceNumberPage)
  val fileUploadState                = userAnswers.fileUploadState
  val indirectRepresentativePage     = userAnswers.get(IndirectRepresentativePage)
  val agentImporterHasEORIPage       = userAnswers.get(AgentImporterHasEORIPage)
  val whomToPayPage                  = userAnswers.get(WhomToPayPage)
  val repaymentTypePage              = userAnswers.get(RepaymentTypePage)
  val emailAddressAndPhoneNumberPage = userAnswers.get(EmailAddressAndPhoneNumberPage)
  val declarantReferenceNumberPage   = userAnswers.get(DeclarantReferenceNumberPage)
  val importerHasEoriPage            = userAnswers.get(ImporterHasEoriPage)
  val claimantTypePage               = userAnswers.get(ClaimantTypePage)
  val numberOfEntriesTypePage        = userAnswers.get(NumberOfEntriesTypePage)
  val entryDetailsPage               = userAnswers.get(EntryDetailsPage)
  val customsRegulationTypePage      = userAnswers.get(CustomsRegulationTypePage)
  val claimIdQuery                   = userAnswers.get(ClaimIdQuery)
  val claimDateQuery                 = userAnswers.get(ClaimDateQuery)

  def getCheckYourAnswerSections: Seq[AnswerSection] =
    Seq(getImportantInformationAnswerSection, getEntryDetailsAnswerSection, getApplicationInformationAnswerSection) ++
      (claimantTypePage.contains(ClaimantType.Representative) match {
        case true => Seq(getImporterDetailsAnswerSection)
        case _    => Nil
      }) ++
      Seq(getYourDetailsAnswerSection, getContactDetailsAnswerSection, getPaymentInformationAnswerSection)

  def getCreateConfirmationSections: Seq[AnswerSection] = Seq(getApplicationSentSection) ++ getCheckYourAnswerSections

  def getAmendCheckYourAnswerSections: Seq[AnswerSection] = {
    def additionalSection = amendCaseResponseTypePage.map { amendCaseResponseType =>
      (amendCaseResponseType.contains(FurtherInformation), amendCaseResponseType.contains(SupportingDocuments)) match {
        case (true, true) if furtherInformationCheck.nonEmpty =>
          Seq(furtherInformationCheck.get, evidenceFileUploadsAmend)
        case (false, true)                                     => Seq(evidenceFileUploadsAmend)
        case (true, false) if furtherInformationCheck.nonEmpty => getAnswerRow(furtherInformationCheck)
        case (_, _)                                            => Nil
      }
    }.getOrElse(Nil)

    val list = (referenceNumberCheck.nonEmpty, amendCaseResponseTypeCheck.nonEmpty) match {
      case (true, true)  => Seq(referenceNumberCheck.get, amendCaseResponseTypeCheck.get)
      case (true, false) => getAnswerRow(referenceNumberCheck)
      case (false, true) => getAnswerRow(amendCaseResponseTypeCheck)
      case (_, _)        => Nil
    }

    Seq(AnswerSection(Some(messages("")), list ++ additionalSection))
  }

  private def bulkFileUpload: Option[AnswerRow] =
    fileUploadState.map(_.fileUploads.files.filter(_.fileType.contains(Bulk))).flatMap(_.headOption.map(_ match {
      case Accepted(_, _, _, _, _, fileName, _, _) =>
        AnswerRow(
          HtmlFormat.escape(messages("bulkFileUpload.checkYourAnswersLabel")),
          HtmlFormat.escape(messages(s"$fileName")),
          Some(routes.CheckYourAnswersController.onChange(BulkFileUploadPage).url)
        )
      case _ =>
        AnswerRow(
          HtmlFormat.escape(messages("bulkFileUpload.checkYourAnswersLabel")),
          HtmlFormat.escape(messages(s"bulkFileUpload.empty")),
          Some(routes.CheckYourAnswersController.onChange(BulkFileUploadPage).url)
        )
    }))

  private def proofOfAuthority: Option[AnswerRow] =
    fileUploadState.map(_.fileUploads.files.filter(_.fileType.contains(ProofOfAuthority))).flatMap(
      _.headOption.map(_ match {
        case Accepted(_, _, _, _, _, fileName, _, _) =>
          AnswerRow(
            HtmlFormat.escape(messages("proofOfAuthority.checkYourAnswersLabel")),
            HtmlFormat.escape(messages(s"$fileName")),
            Some(routes.CheckYourAnswersController.onChange(ProofOfAuthorityPage).url)
          )
        case _ =>
          AnswerRow(
            HtmlFormat.escape(messages("proofOfAuthority.checkYourAnswersLabel")),
            HtmlFormat.escape(messages(s"proofOfAuthority.empty")),
            Some(routes.CheckYourAnswersController.onChange(ProofOfAuthorityPage).url)
          )
      })
    )

  private def indirectRepresentative: Option[AnswerRow] = indirectRepresentativePage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("indirectRepresentative.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.CheckYourAnswersController.onChange(IndirectRepresentativePage).url)
      )
  }

  private def bankDetails: Option[AnswerRow] = userAnswers.get(BankDetailsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("bankDetails.checkYourAnswersLabel")),
        Html(
          Seq(
            HtmlFormat.escape(x.AccountName).toString,
            HtmlFormat.escape(x.SortCode).toString,
            HtmlFormat.escape(x.AccountNumber).toString
          ).mkString("<br>")
        ),
        Some(routes.CheckYourAnswersController.onChange(BankDetailsPage).url)
      )
  }

  private def agentImporterAddress: Option[AnswerRow] = userAnswers.get(AgentImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterAddress.checkYourAnswersLabel")),
        formatAddress(x),
        Some(routes.CheckYourAnswersController.onChange(AgentImporterAddressPage).url)
      )
  }

  private def importerAddress: Option[AnswerRow] = userAnswers.get(ImporterAddressPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(claimantTypePage match {
          case Some(ClaimantType.Importer) => messages("agentImporterAddress.checkYourAnswersLabel")
          case _                           => messages("importerAddress.checkYourAnswersLabel")
        }),
        formatAddress(x),
        Some(routes.CheckYourAnswersController.onChange(ImporterAddressPage).url)
      )
  }

  private def formatAddress(x: Address) =
    Html(
      Seq(
        HtmlFormat.escape(x.AddressLine1).toString,
        HtmlFormat.escape(x.AddressLine2.getOrElse("")).toString,
        HtmlFormat.escape(x.City).toString,
        HtmlFormat.escape(x.Region.getOrElse("")).toString,
        HtmlFormat.escape(x.PostalCode.getOrElse("")).toString,
        HtmlFormat.escape(x.Country.name).toString
      ).filter(!_.isEmpty()).mkString("<br>")
    )

  private def agentImporterHasEORI: Option[AnswerRow] = agentImporterHasEORIPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("agentImporterHasEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"agentImporterHasEORI.$x")),
        Some(routes.CheckYourAnswersController.onChange(AgentImporterHasEORIPage).url)
      )
  }

  private def isImporterVatRegistered: Option[AnswerRow] = userAnswers.get(IsImporterVatRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isImporterVatRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isImporterVatRegistered.$x")),
        Some(routes.CheckYourAnswersController.onChange(IsImporterVatRegisteredPage).url)
      )
  }

  private def enterAgentEORI: Option[AnswerRow] = userAnswers.get(EnterAgentEORIPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("enterAgentEORI.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.CheckYourAnswersController.onChange(EnterAgentEORIPage).url)
      )
  }

  private def whomToPay: Option[AnswerRow] = whomToPayPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("whomToPay.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"whomToPay.$x")),
        Some(routes.CheckYourAnswersController.onChange(WhomToPayPage).url)
      )
  }

  private def repaymentType: Option[AnswerRow] = repaymentTypePage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("repaymentType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"repaymentType.$x")),
        Some(routes.CheckYourAnswersController.onChange(RepaymentTypePage).url)
      )
  }

  private def representativeImporterName: Option[AnswerRow] = userAnswers.get(RepresentativeImporterNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("representative.importer.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.CheckYourAnswersController.onChange(RepresentativeImporterNamePage).url)
      )
  }

  private def phoneNumber: Option[AnswerRow] = emailAddressAndPhoneNumberPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("phoneNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x.phone.getOrElse("")),
        Some(routes.CheckYourAnswersController.onChange(EmailAddressAndPhoneNumberPage).url)
      )
  }

  private def emailAddress: Option[AnswerRow] = emailAddressAndPhoneNumberPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("emailAddress.checkYourAnswersLabel")),
        HtmlFormat.escape(x.email.getOrElse("")),
        Some(routes.CheckYourAnswersController.onChange(EmailAddressAndPhoneNumberPage).url)
      )
  }

  private def declarantReferenceNumber: Option[AnswerRow] = declarantReferenceNumberPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("declarantReferenceNumber.checkYourAnswersLabel.answer")),
        HtmlFormat.escape(x.declarantReferenceNumber.getOrElse("")),
        Some(routes.CheckYourAnswersController.onChange(DeclarantReferenceNumberPage).url)
      )
  }

  private def declarantReferenceNumberQuestion: Seq[AnswerRow] =
    declarantReferenceNumberPage match {
      case Some(v) if v.declarantReferenceType == Yes =>
        Seq(
          Some(
            AnswerRow(
              HtmlFormat.escape(messages("declarantReferenceNumber.checkYourAnswersLabel.question")),
              HtmlFormat.escape(changeToYesNo(Yes.toString)),
              Some(routes.CheckYourAnswersController.onChange(DeclarantReferenceNumberPage).url)
            )
          ),
          declarantReferenceNumber
        ).flatten
      case _ =>
        Seq(
          AnswerRow(
            HtmlFormat.escape(messages("declarantReferenceNumber.checkYourAnswersLabel.question")),
            HtmlFormat.escape(changeToYesNo(No.toString)),
            Some(routes.CheckYourAnswersController.onChange(DeclarantReferenceNumberPage).url)
          )
        )
    }

  private def declarantName: Option[AnswerRow] = userAnswers.get(DeclarantNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("declarantName.checkYourAnswersLabel")),
        HtmlFormat.escape(x.firstName.concat(" ").concat(x.lastName)),
        Some(routes.CheckYourAnswersController.onChange(DeclarantNamePage).url)
      )
  }

  private def claimRepaymentType: Option[AnswerRow] = userAnswers.get(ClaimRepaymentTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimRepaymentType.checkYourAnswersLabel")),
        Html(x.map(value => HtmlFormat.escape(messages(s"claimRepaymentType.$value")).toString).mkString(",<br>")),
        Some(routes.CheckYourAnswersController.onChange(ClaimRepaymentTypePage).url)
      )
  }

  private def reasonForOverpayment: Option[AnswerRow] = userAnswers.get(ReasonForOverpaymentPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("reasonForOverpayment.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.CheckYourAnswersController.onChange(ReasonForOverpaymentPage).url)
      )
  }

  private def claimReasonType: Option[AnswerRow] = userAnswers.get(ClaimReasonTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimReasonType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"claimReasonType.$x")),
        Some(routes.CheckYourAnswersController.onChange(ClaimReasonTypePage).url)
      )
  }

  private def agentName: Option[AnswerRow] = userAnswers.get(RepresentativeDeclarantAndBusinessNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("representative.declarantAndBusinessName.agentName.checkYourAnswersLabel")),
        HtmlFormat.escape(x.agentName),
        Some(routes.CheckYourAnswersController.onChange(RepresentativeDeclarantAndBusinessNamePage).url)
      )
  }

  private def representativeDeclarantName: Option[AnswerRow] =
    userAnswers.get(RepresentativeDeclarantAndBusinessNamePage) map {
      x =>
        AnswerRow(
          HtmlFormat.escape(messages("representative.declarantAndBusinessName.declarantName.checkYourAnswersLabel")),
          HtmlFormat.escape(x.declarantName),
          Some(routes.CheckYourAnswersController.onChange(RepresentativeDeclarantAndBusinessNamePage).url)
        )
    }

  private def doYouOwnTheGoods(declarantName: String): Option[AnswerRow] = userAnswers.get(DoYouOwnTheGoodsPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("doYouOwnTheGoods.checkYourAnswersLabel", declarantName)),
        HtmlFormat.escape(changeToYesNo(x.toString)),
        Some(routes.CheckYourAnswersController.onChange(DoYouOwnTheGoodsPage).url)
      )
  }

  private def goodsOwnerName: Option[AnswerRow] = userAnswers.get(ImporterNamePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerName.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.CheckYourAnswersController.onChange(ImporterNamePage).url)
      )
  }

  private def repaymentAmountSummary: AnswerRow = {
    val helper = new RepaymentAmountSummaryAnswersHelper(userAnswers)
    AnswerRow(
      HtmlFormat.escape(messages("repaymentAmountSummary.total.checkYourAnswersLabel")),
      HtmlFormat.escape("£" + helper.getTotalAmount().format2d),
      Some(routes.RepaymentAmountSummaryController.onPageLoad().url)
    )
  }

  private def evidenceFileUploads: AnswerRow = {
    val noOfDocuments = fileUploadState.map(_.fileUploads.acceptedCount).getOrElse(0)
    AnswerRow(
      HtmlFormat.escape(messages("view.upload-file.checkYourAnswersLabel")),
      HtmlFormat.escape(
        (noOfDocuments.toString)
          .concat(" ").concat(messages("view.upload-file.documents.added"))
      ),
      Some(routes.CheckYourAnswersController.onChange(FileUploadedPage).url)
    )
  }

  private def getImportantInformationAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("impInfo.checkYourAnswersLabel")),
      getAnswerRow(claimantType) ++
        getAnswerRow(numberOfEntriesType) ++
        getAnswerRow(customsRegulationType) ++
        (customsRegulationTypePage match {
          case Some(CustomsRegulationType.UnionsCustomsCodeRegulation) => getAnswerRow(articleType)
          case _                                                       => getAnswerRow(ukRegulationType)
        })
    )

  private def getApplicationSentSection: AnswerSection =
    AnswerSection(
      Some(messages("confirmation.summary.section.heading")),
      getAnswerRow(claimId) ++
        getAnswerRow(claimDate)
    )

  private def claimId: Option[AnswerRow] = claimIdQuery map {
    x =>
      AnswerRow(HtmlFormat.escape(messages("confirmation.summary.reference")), HtmlFormat.escape(x))
  }

  private def claimDate: Option[AnswerRow] = claimDateQuery map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("confirmation.summary.datetime")),
        HtmlFormat.escape(DateTimeFormats.formatDateAtTime(x))
      )
  }

  private def getAnswerRow(answerRow: Option[AnswerRow]) = answerRow.map(Seq(_)).getOrElse(Nil)

  private def getEntryDetailsAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("entryDetails.checkYourAnswersLabel")),
      Nil ++
        (numberOfEntriesTypePage match {
          case Some(v) if v.numberOfEntriesType == Multiple => getAnswerRow(bulkFileUpload)
          case _                                            => Nil
        }) ++
        getAnswerRow(entryDetailsEPU) ++
        getAnswerRow(entryDetailsNumber) ++
        getAnswerRow(entryDetailsDate)
    )

  private def getApplicationInformationAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("applicationInformation.checkYourAnswersLabel")),
      getAnswerRow(claimReasonType) ++
        getAnswerRow(reasonForOverpayment) ++
        getAnswerRow(claimRepaymentType) ++
        Seq(repaymentAmountSummary, evidenceFileUploads)
    )

  private def getImporterDetailsAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("importer.details.checkYourAnswersLabel")),
      getAnswerRow(agentImporterHasEORI) ++
        (agentImporterHasEORIPage match {
          case Some(AgentImporterHasEORI.Yes) => getAnswerRow(enterAgentEORI)
          case _                              => Nil
        }) ++
        getAnswerRow(isImporterVatRegistered) ++
        getAnswerRow(representativeImporterName) ++
        getAnswerRow(importerAddress)
    )

  private def getYourDetailsAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("your.details.checkYourAnswersLabel")),
      getAnswerRow(importerHasEori) ++
        (importerHasEoriPage match {
          case Some(v) if v => getAnswerRow(importerEori)
          case _            => Nil
        }) ++
        (claimantTypePage.contains(ClaimantType.Importer) match {
          case true => getAnswerRow(isVATRegistered)
          case _    => Nil
        }) ++

        (claimantTypePage.contains(ClaimantType.Importer) match {
          case true =>
            getAnswerRow(declarantName) ++
              getAnswerRow(doYouOwnTheGoods(userAnswers.get(DeclarantNamePage).map(_.toString).getOrElse(""))) ++
              getAnswerRow(goodsOwnerName)
          case _ => getAnswerRow(representativeDeclarantName) ++ getAnswerRow(agentName)
        }) ++
        getAnswerRow(claimantTypePage match {
          case Some(ClaimantType.Importer) =>
            importerAddress
          case _ =>
            agentImporterAddress
        })
    )

  private def getContactDetailsAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("contact.details.checkYourAnswersLabel")),
      (emailAddressAndPhoneNumberPage match {
        case Some(v) if v.phone.isEmpty => Nil
        case _                          => getAnswerRow(phoneNumber)
      }) ++
        (emailAddressAndPhoneNumberPage match {
          case Some(v) if v.email.isEmpty => Nil
          case _                          => getAnswerRow(emailAddress)
        }) ++
        declarantReferenceNumberQuestion
    )

  private def getPaymentInformationAnswerSection: AnswerSection =
    AnswerSection(
      Some(messages("payment.information.checkYourAnswersLabel")),
      Nil ++
        ((repaymentTypePage.contains(RepaymentType.BACS)
          || repaymentTypePage.isEmpty)
          &&
            claimantTypePage.contains(ClaimantType.Representative) match {
          case true =>
            (whomToPayPage match {
              case None => Nil
              case _    => getAnswerRow(whomToPay)
            }) ++
              (whomToPayPage.contains(WhomToPay.Representative) match {
                case true => getAnswerRow(indirectRepresentative)
                case _    => Nil
              }) ++
              ((indirectRepresentativePage, whomToPayPage) match {
                case (Some(false), Some(WhomToPay.Representative)) => getAnswerRow(proofOfAuthority)
                case _                                             => Nil
              })
          case _ => Nil
        }) ++
        (repaymentTypePage match {
          case None => Nil
          case _ =>
            numberOfEntriesTypePage match {
              case Some(v) if v.numberOfEntriesType == Single => getAnswerRow(repaymentType)
              case _                                          => Nil
            }
        }) ++
        (repaymentTypePage.contains(RepaymentType.CMA) match {
          case true => Nil
          case _    => getAnswerRow(bankDetails)
        })
    )

  private def entryDetailsEPU: Option[AnswerRow] = entryDetailsPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.epu.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EPU)),
        Some(routes.CheckYourAnswersController.onChange(EntryDetailsPage).url)
      )
  }

  private def entryDetailsNumber: Option[AnswerRow] = entryDetailsPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.number.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(x.EntryNumber)),
        Some(routes.CheckYourAnswersController.onChange(EntryDetailsPage).url)
      )
  }

  private def entryDetailsDate: Option[AnswerRow] = entryDetailsPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("entryDetails.date.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(DateTimeFormats.formatDate(x.EntryDate))),
        Some(routes.CheckYourAnswersController.onChange(EntryDetailsPage).url)
      )
  }

  private def numberOfEntriesType: Option[AnswerRow] = numberOfEntriesTypePage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("numberOfEntriesType.checkYourAnswersLabel")),
        HtmlFormat.escape(numberOfEntriesTypePage match {
          case Some(v) if v.numberOfEntriesType == Single   => "1"
          case Some(v) if v.numberOfEntriesType == Multiple => v.entries.getOrElse("")
        }),
        Some(routes.CheckYourAnswersController.onChange(NumberOfEntriesTypePage).url)
      )
  }

  private def articleType: Option[AnswerRow] = userAnswers.get(ArticleTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("articleType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"articleType.$x")),
        Some(routes.CheckYourAnswersController.onChange(ArticleTypePage).url)
      )
  }

  private def ukRegulationType: Option[AnswerRow] = userAnswers.get(UkRegulationTypePage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("ukRegulationType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"ukRegulationType.$x")),
        Some(routes.CheckYourAnswersController.onChange(UkRegulationTypePage).url)
      )
  }

  private def customsRegulationType: Option[AnswerRow] = customsRegulationTypePage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("customsRegulationType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"customsRegulationType.$x")),
        Some(routes.CheckYourAnswersController.onChange(CustomsRegulationTypePage).url)
      )
  }

  private def isVATRegistered: Option[AnswerRow] = userAnswers.get(IsVATRegisteredPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("isVATRegistered.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(s"isVATRegistered.$x")),
        Some(routes.CheckYourAnswersController.onChange(IsVATRegisteredPage).url)
      )
  }

  private def importerEori: Option[AnswerRow] = userAnswers.get(ImporterEoriPage) map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerEori.checkYourAnswersLabel")),
        HtmlFormat.escape(x.value),
        Some(routes.CheckYourAnswersController.onChange(ImporterEoriPage).url)
      )
  }

  private def importerHasEori: Option[AnswerRow] = importerHasEoriPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("importerHasEori.checkYourAnswersLabel")),
        yesOrNo(x),
        Some(routes.CheckYourAnswersController.onChange(ImporterHasEoriPage).url)
      )
  }

  private def claimantType: Option[AnswerRow] = claimantTypePage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("claimantType.checkYourAnswersLabel")),
        HtmlFormat.escape(claimantTypePage match {
          case Some(ClaimantType.Importer) => messages("claimantType.importer")
          case _                           => messages("claimantType.representative")
        }),
        Some(routes.CheckYourAnswersController.onChange(ClaimantTypePage).url)
      )
  }

  private def amendCaseResponseTypeCheck: Option[AnswerRow] = amendCaseResponseTypePage map {
    x =>
      val message =
        if (x.contains(FurtherInformation) && x.contains(SupportingDocuments))
          messages("amendCaseResponseType.sendInformationAndDocuments.checkYourAnswersLabel")
        else if (x.contains(FurtherInformation)) messages("amendCaseResponseType.sendInformation.checkYourAnswersLabel")
        else messages("amendCaseResponseType.sendDocuments.checkYourAnswersLabel")

      AnswerRow(
        HtmlFormat.escape(messages("amendCaseResponseType.checkYourAnswersLabel")),
        HtmlFormat.escape(messages(message)),
        Some(routes.AmendCheckYourAnswersController.onChange(AmendCaseResponseTypePage).url)
      )
  }

  private def furtherInformationCheck: Option[AnswerRow] = furtherInformationPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("furtherInformation.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.AmendCheckYourAnswersController.onChange(FurtherInformationPage).url)
      )
  }

  private def referenceNumberCheck: Option[AnswerRow] = referenceNumberPage map {
    x =>
      AnswerRow(
        HtmlFormat.escape(messages("referenceNumber.checkYourAnswersLabel")),
        HtmlFormat.escape(x),
        Some(routes.AmendCheckYourAnswersController.onChange(ReferenceNumberPage).url)
      )
  }

  private def evidenceFileUploadsAmend: AnswerRow = {
    val noOfDocuments = fileUploadState.map(_.fileUploads.acceptedCount).getOrElse(0)
    AnswerRow(
      HtmlFormat.escape(messages("view.amend-upload-file.checkYourAnswersLabel")),
      if (noOfDocuments == 1)
        HtmlFormat.escape(messages("view.amend-upload-file.document.added", noOfDocuments))
      else
        HtmlFormat.escape(messages("view.amend-upload-file.documents.added", noOfDocuments)),
      Some(routes.AmendCheckYourAnswersController.onChange(AmendFileUploadedPage).url)
    )
  }

  private def yesOrNo(answer: Boolean)(implicit messages: Messages): Html =
    if (answer)
      HtmlFormat.escape(messages("site.yes"))
    else
      HtmlFormat.escape(messages("site.no"))

  implicit class Improvements(s: Double) {
    def format2d = "%.2f".format(s)
  }

  private def changeToYesNo(string: String): String =
    string match {
      case "01" => "Yes"
      case "02" => "No"
    }

}
