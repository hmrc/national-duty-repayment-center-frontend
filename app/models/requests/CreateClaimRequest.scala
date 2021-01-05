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

import models.WhomToPay.{Importer, Representative}
import models._
import pages._
import play.api.libs.json.{Json, OFormat}

final case class CreateClaimRequest(
                                     Content: Content
                                   )

object CreateClaimRequest {
  implicit val formats: OFormat[CreateClaimRequest] = Json.format[CreateClaimRequest]

  def buildValidClaimRequest(userAnswers: UserAnswers): Option[CreateClaimRequest] = {

    def getArticleType(userAnswers: UserAnswers): Option[ArticleType] = {
      userAnswers.get(CustomsRegulationTypePage) match {
        case Some(CustomsRegulationType.UKCustomsCodeRegulation) => Some(ArticleType.Schedule)
        case _ => Some(userAnswers.get(ArticleTypePage).get)
      }
    }

    def getClaimDetails(userAnswers: UserAnswers): Option[ClaimDetails] = for {
      customRegulationType <- userAnswers.get(CustomsRegulationTypePage)
      claimedUnderArticle <- getArticleType(userAnswers)
      claimant <- userAnswers.get(ClaimantTypePage)
      claimType <- userAnswers.get(NumberOfEntriesTypePage)
      noOfEntries <- Some(userAnswers.get(HowManyEntriesPage))
      entryDetails <- userAnswers.get(EntryDetailsPage)
      claimReason <- userAnswers.get(ClaimReasonTypePage)
      claimDescription <- userAnswers.get(ReasonForOverpaymentPage)
      payeeIndicator <- userAnswers.get(WhomToPayPage)
      paymentMethod <- userAnswers.get(RepaymentTypePage)
    } yield ClaimDetails(FormType("01"),
      customRegulationType,
      claimedUnderArticle,
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
      "NA"
    )

    def getAgentIsVATRegistered(userAnswers: UserAnswers): Option[String] = userAnswers.get(IsImporterVatRegisteredPage) match {
      case Some(true) => Some("true")
      case Some(false) => Some("false")
      case _ => None
    }

    def getAgentUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      isVATRegistered <- getAgentIsVATRegistered(userAnswers)
      eori <- userAnswers.get(EnterAgentEORIPage)
      name <- userAnswers.get(AgentNameImporterPage)
      address <- userAnswers.get(AgentImporterAddressPage)
      telephone <- userAnswers.get(PhoneNumberPage)
      email <- userAnswers.get(EmailAddressPage)
    } yield UserDetails(
      isVATRegistered,
      eori,
      name,
      address,
      Some(telephone),
      Some(email)
    )

    def getImporterIsVATRegistered(userAnswers: UserAnswers): Option[String] = userAnswers.get(IsVATRegisteredPage) match {
      case Some(true) => Some("true")
      case Some(false) => Some("false")
      case _ => None
    }

    def getImporterAddress(userAnswers: UserAnswers): Option[Address] = userAnswers.get(ImporterAddressPage) match {
      case Some(_) => userAnswers.get(ImporterAddressPage)
      case _ => userAnswers.get(ImporterManualAddressPage)
    }

    def getImporterUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      isVATRegistered <- getImporterIsVATRegistered(userAnswers)
      name <- userAnswers.get(ImporterNamePage)
      address <- getImporterAddress(userAnswers)
    } yield {
      val eori = userAnswers.get(ImporterEoriPage).getOrElse(EORI("GBPR"))
      val email = "test email" //TODO need to get email from ContactByEmailPage
      val telephone = userAnswers.get(PhoneNumberPage)
      UserDetails(
        isVATRegistered,
        eori,
        name,
        address,
        telephone,
        Some(email)
      )
    }

    def getBankDetails(userAnswers: UserAnswers): Option[AllBankDetails] = userAnswers.get(RepaymentTypePage) match {
      case Some(RepaymentType.BACS) =>
        for {
          bankDetails <- userAnswers.get(BankDetailsPage)
        } yield (userAnswers.get(ClaimantTypePage), userAnswers.get(WhomToPayPage)) match {
          case (Some(models.ClaimantType.Importer), None) | (Some(models.ClaimantType.Representative), Some(Importer)) =>
            AllBankDetails(ImporterBankDetails = Some(bankDetails), AgentBankDetails = None)
          case _ =>
            AllBankDetails(ImporterBankDetails = None, AgentBankDetails = Some(bankDetails))
        }
      case _ => None
    }

    def cleanseBankDetails(bankDetails: BankDetails): BankDetails = bankDetails.copy(
      SortCode = bankDetails.sortCodeTrimmed,
      AccountNumber = bankDetails.accountNumberPadded
    )

    def getCustomsValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getCustomsDutyPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Customs) match {
        case true => userAnswers.get(CustomsDutyPaidPage)
        case _ => Some("0.0")
      }

      val getCustomsDutyDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Customs) match {
        case true => userAnswers.get(CustomsDutyDueToHMRCPage)
        case _ => Some("0.0")
      }

      val CustomsDutyPaidAsDouble = getCustomsDutyPaid.getOrElse("0.0").toDouble
      val CustomsDutyDueAsDouble = getCustomsDutyDue.getOrElse("0.0").toDouble

      val CustomsDutyOwedAsString = (CustomsDutyPaidAsDouble - CustomsDutyDueAsDouble).toString

      DutyTypeTaxList(ClaimRepaymentType.Customs, getCustomsDutyPaid.getOrElse("0.0"), getCustomsDutyDue.getOrElse("0.0"), CustomsDutyOwedAsString)
    }

    def getVatValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getVatPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Vat) match {
        case true => userAnswers.get(VATDueToHMRCPage)
        case _ => Some("0.0")
      }

      val getVatDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Vat) match {
        case true => userAnswers.get(VATPaidPage)
        case _ => Some("0.0")
      }

      val VatPaidAsDouble = getVatPaid.getOrElse("0.0").toDouble
      val VatDueAsDouble = getVatDue.getOrElse("0.0").toDouble

      val VatOwedAsString = (VatPaidAsDouble - VatDueAsDouble).toString

      DutyTypeTaxList(ClaimRepaymentType.Vat, getVatPaid.getOrElse("0.0"), getVatDue.getOrElse("0.0"), VatOwedAsString)
    }

    def getOtherDutyValues(userAnswers: UserAnswers): DutyTypeTaxList = {

      val selectedDuties: Set[ClaimRepaymentType] = userAnswers.get(ClaimRepaymentTypePage).get

      val getOtherDutyPaid: Option[String] = selectedDuties.contains(ClaimRepaymentType.Other) match {
        case true => userAnswers.get(OtherDutiesPaidPage)
        case _ => Some("0.0")
      }

      val getOtherDutyDue: Option[String] = selectedDuties.contains(ClaimRepaymentType.Other) match {
        case true => userAnswers.get(OtherDutiesDueToHMRCPage)
        case _ => Some("0.0")
      }

      val OtherDutyPaidAsDouble = getOtherDutyPaid.getOrElse("0.0").toDouble
      val OtherDutyDueAsDouble = getOtherDutyDue.getOrElse("0.0").toDouble

      val OtherDutyOwedAsString = (OtherDutyPaidAsDouble - OtherDutyDueAsDouble).toString

      DutyTypeTaxList(ClaimRepaymentType.Other, getOtherDutyPaid.getOrElse("0.0"), getOtherDutyDue.getOrElse("0.0"), OtherDutyOwedAsString)
    }

    def getDutyTypeTaxDetails(answers: UserAnswers): Seq[DutyTypeTaxList] = {
      Seq(getCustomsValues(answers), getVatValues(answers), getOtherDutyValues(answers))
    }

    def getTypeTaxDetails(userAnswers: UserAnswers): DutyTypeTaxDetails = {
      DutyTypeTaxDetails(getDutyTypeTaxDetails(userAnswers))
    }

    def getDocumentList(): DocumentList = {
      DocumentList(EvidenceSupportingDocs.Other, None)
    }

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails <- getClaimDetails(userAnswers)
      importerDetails <- getImporterUserDetails(userAnswers)
      bankDetails <- getBankDetails(userAnswers)
    } yield {
      val documentList: DocumentList = getDocumentList()
      val agentDetails: Option[UserDetails] = userAnswers.get(ClaimantTypePage) match {
        case Some(models.ClaimantType.Importer) => None
        case _ => getAgentUserDetails(userAnswers)
      }

      val dutyTypeTaxDetails = getTypeTaxDetails(userAnswers)

      Content(
        claimDetails,
        agentDetails,
        importerDetails,
        Some(bankDetails),
        dutyTypeTaxDetails,
        Seq(documentList)
      )
    }


    for {
      content <- getContent(userAnswers)
    } yield CreateClaimRequest(
      content
    )
  }
}

