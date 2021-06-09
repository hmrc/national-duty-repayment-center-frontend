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

package models.requests

import java.time.LocalDate

import models.DeclarantReferenceType.{No, Yes}
import models.WhomToPay.Importer
import models._
import models.eis.EISAddress
import pages._
import play.api.libs.json.{Json, OFormat}

final case class CreateClaimRequest(Content: Content, uploadedFiles: Seq[UploadedFile])

object CreateClaimRequest {
  implicit val formats: OFormat[CreateClaimRequest] = Json.format[CreateClaimRequest]

  def buildValidClaimRequest(userAnswers: UserAnswers): Option[CreateClaimRequest] = {

    def getPayeeIndicator(userAnswers: UserAnswers): Option[WhomToPay] =
      userAnswers.get(ClaimantTypePage) match {
        case Some(ClaimantType.Importer) =>
          userAnswers.get(NumberOfEntriesTypePage).get.numberOfEntriesType match {
            case NumberOfEntriesType.Single =>
              userAnswers.get(RepaymentTypePage) match {
                case Some(RepaymentType.CMA)  => Some(WhomToPay.CMA)
                case Some(RepaymentType.BACS) => Some(WhomToPay.Importer)
              }
            case NumberOfEntriesType.Multiple => Some(WhomToPay.Importer)
          }
        case Some(ClaimantType.Representative) =>
          userAnswers.get(NumberOfEntriesTypePage).get.numberOfEntriesType match {
            case NumberOfEntriesType.Single =>
              userAnswers.get(RepaymentTypePage) match {
                case Some(RepaymentType.CMA)  => Some(WhomToPay.CMA)
                case Some(RepaymentType.BACS) => userAnswers.get(WhomToPayPage)
              }
            case _ => userAnswers.get(WhomToPayPage)
          }
      }

    def getPaymentMethod(userAnswers: UserAnswers): Option[RepaymentType] =
      userAnswers.get(NumberOfEntriesTypePage).get.numberOfEntriesType match {
        case NumberOfEntriesType.Multiple => Some(RepaymentType.BACS)
        case NumberOfEntriesType.Single   => userAnswers.get(RepaymentTypePage)
      }

    def getClaimDetails(userAnswers: UserAnswers): Option[ClaimDetails] = for {
      customRegulationType   <- userAnswers.get(CustomsRegulationTypePage)
      claimedUnderArticle    <- Some(userAnswers.get(ArticleTypePage))
      claimedUnderRegulation <- Some(userAnswers.get(UkRegulationTypePage))
      claimant               <- userAnswers.get(ClaimantTypePage)
      claimType              <- userAnswers.get(NumberOfEntriesTypePage).map(_.numberOfEntriesType)
      noOfEntries            <- userAnswers.get(NumberOfEntriesTypePage).map(_.entries)
      entryDetails           <- userAnswers.get(EntryDetailsPage)
      claimReason            <- userAnswers.get(ClaimReasonTypePage)
      claimDescription       <- userAnswers.get(ReasonForOverpaymentPage)
      payeeIndicator         <- getPayeeIndicator(userAnswers)
      paymentMethod          <- getPaymentMethod(userAnswers)
      declarantReNumber      <- getDecRef(userAnswers)
      declarantName          <- getDeclarantName(userAnswers)
    } yield ClaimDetails(
      FormType("01"),
      customRegulationType,
      claimedUnderArticle,
      claimedUnderRegulation,
      claimant,
      claimType,
      noOfEntries,
      entryDetails,
      claimReason,
      claimDescription,
      LocalDate.now(),
      LocalDate.now(),
      payeeIndicator,
      paymentMethod,
      declarantReNumber,
      declarantName
    )

    def getDeclarantName(userAnswers: UserAnswers): Option[String] = userAnswers.get(ClaimantTypePage) match {
      case Some(ClaimantType.Importer) => userAnswers.get(DeclarantNamePage).map(_.toString)
      case _                           => userAnswers.get(RepresentativeDeclarantAndBusinessNamePage).map(_.declarantName)
    }

    def getAgentImporterAddress(userAnswers: UserAnswers): Option[Address] = userAnswers.get(AgentImporterAddressPage)
    def getDecRef(userAnswers: UserAnswers): Option[String] = userAnswers.get(DeclarantReferenceNumberPage) match {
      case Some(decRef) if decRef.declarantReferenceType == Yes => Some(decRef.declarantReferenceNumber.get)
      case Some(decRef) if decRef.declarantReferenceType == No  => Some("NA")
    }

    def getEmailAddress(userAnswers: UserAnswers): Option[String] =
      userAnswers.get(EmailAddressAndPhoneNumberPage) match {
        //case Some(email) if email.length > 0 => Some(email)
        case Some(emailAndPhone) if emailAndPhone.email.map(_.length).getOrElse(0) > 0 => Some(emailAndPhone.email.get)
        case _                                                                         => None
      }

    def getTelePhone(userAnswers: UserAnswers): Option[String] = userAnswers.get(EmailAddressAndPhoneNumberPage) match {
      case Some(emailAndPhone) if emailAndPhone.phone.map(_.length).getOrElse(0) > 0 => Some(emailAndPhone.phone.get)
      case _                                                                         => None
    }

    def getAgentUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      name    <- getAgentImporterName(userAnswers)
      address <- getAgentImporterAddress(userAnswers)
    } yield {
      val eori      = userAnswers.get(ImporterEoriPage).getOrElse(EORI("GBPR"))
      val telephone = getTelePhone(userAnswers)
      val email     = getEmailAddress(userAnswers)
      UserDetails("false", eori, name, EISAddress(address), telephone, email)
    }

    def getIsVATRegistered(userAnswers: UserAnswers): String = userAnswers.get(IsVATRegisteredPage) match {
      case Some(IsVATRegistered.Yes) => "true"
      case _                         => "false"
    }

    def getIsImporterVatRegistered(userAnswers: UserAnswers): String =
      userAnswers.get(IsImporterVatRegisteredPage) match {
        case Some(IsImporterVatRegistered.Yes) => "true"
        case _                                 => "false"
      }

    def getImporterAddress(userAnswers: UserAnswers): Option[Address] = userAnswers.get(ImporterAddressPage)

    def getAgentImporterName(userAnswers: UserAnswers): Option[String] =
      userAnswers.get(RepresentativeDeclarantAndBusinessNamePage).map(_.agentName)

    def getImporterName(userAnswers: UserAnswers): Option[String] = {
      val declarantNamePage = userAnswers.get(DeclarantNamePage)
      userAnswers.get(ClaimantTypePage) match {
        case Some(ClaimantType.Importer) =>
          userAnswers.get(DoYouOwnTheGoodsPage) match {
            case Some(DoYouOwnTheGoods.No) => userAnswers.get(ImporterNamePage).map(_.value)
            case _                         => declarantNamePage.map(_.toString)
          }
        case _ => userAnswers.get(RepresentativeImporterNamePage).map(_.value)
      }
    }

    def getImporterEORI(userAnswers: UserAnswers): EORI = userAnswers.get(ClaimantTypePage) match {
      case Some(ClaimantType.Importer) => userAnswers.get(ImporterEoriPage).getOrElse(EORI("GBPR"))
      case _                           => userAnswers.get(EnterAgentEORIPage).getOrElse(EORI("GBPR"))
    }

    def getImporterUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      name    <- getImporterName(userAnswers)
      address <- getImporterAddress(userAnswers)
    } yield {
      val eori = getImporterEORI(userAnswers)
      val email = {
        userAnswers.get(ClaimantTypePage) match {
          case Some(ClaimantType.Importer) => getEmailAddress(userAnswers)
          case _                           => None
        }
      }
      val telephone = {
        userAnswers.get(ClaimantTypePage) match {
          case Some(ClaimantType.Importer) => getTelePhone(userAnswers)
          case _                           => None
        }
      }
      val isVATRegistered = {
        userAnswers.get(ClaimantTypePage) match {
          case Some(ClaimantType.Importer) => getIsVATRegistered(userAnswers)
          case _                           => getIsImporterVatRegistered(userAnswers)
        }
      }
      UserDetails(isVATRegistered, eori, name, EISAddress(address), telephone, email)
    }

    def getBankDetails(userAnswers: UserAnswers): Option[AllBankDetails] = getPaymentMethod(userAnswers) match {
      case Some(RepaymentType.BACS) =>
        for {
          bankDetails <- userAnswers.get(BankDetailsPage).map(cleanseBankDetails)
        } yield (userAnswers.get(ClaimantTypePage), getPayeeIndicator(userAnswers)) match {
          case (Some(ClaimantType.Importer), _) | (Some(ClaimantType.Representative), Some(Importer)) =>
            AllBankDetails(ImporterBankDetails = Some(bankDetails), AgentBankDetails = None)
          case _ =>
            AllBankDetails(ImporterBankDetails = None, AgentBankDetails = Some(bankDetails))
        }
      case _ => None
    }

    def cleanseBankDetails(bankDetails: BankDetails): BankDetails =
      bankDetails.copy(SortCode = bankDetails.sortCodeTrimmed, AccountNumber = bankDetails.accountNumberPadded)

    def getCustomsValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getCustomsDutyPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Customs) match {
        case true => userAnswers.get(CustomsDutyPaidPage).map(_.ActualPaidAmount)
        case _    => Some("0.00")
      }

      val getCustomsDutyDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Customs) match {
        case true => userAnswers.get(CustomsDutyPaidPage).map(_.ShouldHavePaidAmount)
        case _    => Some("0.00")
      }

      val CustomsDutyPaidAsBigDecimal = BigDecimal(getCustomsDutyPaid.getOrElse("0.00")).setScale(2)
      val CustomsDutyDueAsBigDecimal  = BigDecimal(getCustomsDutyDue.getOrElse("0.00")).setScale(2)

      val CustomsDutyOwedAsString = (CustomsDutyPaidAsBigDecimal - CustomsDutyDueAsBigDecimal).toString()

      DutyTypeTaxList(
        ClaimRepaymentType.Customs,
        CustomsDutyPaidAsBigDecimal.toString(),
        CustomsDutyDueAsBigDecimal.toString(),
        CustomsDutyOwedAsString
      )
    }

    def getVatValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getVatPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Vat) match {
        case true  => userAnswers.get(VATPaidPage).map(_.ActualPaidAmount)
        case false => Some("0.00")
      }

      val getVatDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Vat) match {
        case true  => userAnswers.get(VATPaidPage).map(_.ShouldHavePaidAmount)
        case false => Some("0.00")
      }

      val VatPaidAsBigDecimal = BigDecimal(getVatPaid.getOrElse("0.00")).setScale(2)
      val VatDueAsBigDecimal  = BigDecimal(getVatDue.getOrElse("0.00")).setScale(2)

      val VatOwedAsString = (VatPaidAsBigDecimal - VatDueAsBigDecimal).toString

      DutyTypeTaxList(
        ClaimRepaymentType.Vat,
        VatPaidAsBigDecimal.toString(),
        VatDueAsBigDecimal.toString,
        VatOwedAsString
      )
    }

    def getOtherDutyValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getOtherDutyPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Other) match {
        case true => userAnswers.get(OtherDutiesPaidPage).map(_.ActualPaidAmount)
        case _    => Some("0.00")
      }

      val getOtherDutyDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Other) match {
        case true => userAnswers.get(OtherDutiesPaidPage).map(_.ShouldHavePaidAmount)
        case _    => Some("0.00")
      }

      val OtherDutyPaidAsBigDecimal = BigDecimal(getOtherDutyPaid.getOrElse("0.0")).setScale(2)
      val OtherDutyDueAsBigDecimal  = BigDecimal(getOtherDutyDue.getOrElse("0.0")).setScale(2)

      val OtherDutyOwedAsString = (OtherDutyPaidAsBigDecimal - OtherDutyDueAsBigDecimal).toString

      DutyTypeTaxList(
        ClaimRepaymentType.Other,
        OtherDutyPaidAsBigDecimal.toString(),
        OtherDutyDueAsBigDecimal.toString(),
        OtherDutyOwedAsString
      )
    }

    def getDutyTypeTaxDetails(answers: UserAnswers): Seq[DutyTypeTaxList] =
      Seq(getCustomsValues(answers), getVatValues(answers), getOtherDutyValues(answers))

    def getTypeTaxDetails(userAnswers: UserAnswers): DutyTypeTaxDetails =
      DutyTypeTaxDetails(getDutyTypeTaxDetails(userAnswers))

    def getDocumentList(): DocumentList =
      DocumentList(EvidenceSupportingDocs.Other, None)

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails    <- getClaimDetails(userAnswers)
      importerDetails <- getImporterUserDetails(userAnswers)
    } yield {
      val documentList: DocumentList = getDocumentList()
      val agentDetails: Option[UserDetails] = userAnswers.get(ClaimantTypePage) match {
        case Some(models.ClaimantType.Importer) => None
        case _                                  => getAgentUserDetails(userAnswers)
      }
      val bankDetails: Option[AllBankDetails] = getBankDetails(userAnswers)
      val dutyTypeTaxDetails                  = getTypeTaxDetails(userAnswers)

      Content(claimDetails, agentDetails, importerDetails, bankDetails, dutyTypeTaxDetails, Seq(documentList))
    }

    for {
      content <- getContent(userAnswers)
    } yield CreateClaimRequest(content, userAnswers.fileUploadState.map(_.fileUploads.toUploadedFiles).getOrElse(Nil))
  }

}
