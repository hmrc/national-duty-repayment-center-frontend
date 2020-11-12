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

package models.requests

import java.time.LocalDate

import models.WhomToPay.{Importer, Representative}
import models._
import pages._
import play.api.libs.json.{Json, OFormat}

final case class CreateClaimRequest(
                                     AcknowledgementReference: AcknowledgementReference,
                                     ApplicationType: ApplicationType,
                                     OriginatingSystem: OriginatingSystem,
                                     Content: Content
                                   )

object CreateClaimRequest {
  implicit val formats: OFormat[CreateClaimRequest] = Json.format[CreateClaimRequest]

  def buildValidClaimRequest(userAnswers: UserAnswers): Option[CreateClaimRequest] = {

    def getClaimDetails(userAnswers: UserAnswers): Option[ClaimDetails] = for {
      customRegulationType <- userAnswers.get(CustomsRegulationTypePage)
      claimedUnderArticle <- userAnswers.get(ArticleTypePage)
      claimant <- userAnswers.get(ClaimantTypePage)
      claimType <- userAnswers.get(NumberOfEntriesTypePage)
      noOfEntries <- Some(userAnswers.get(HowManyEntriesPage))
      epu <- userAnswers.get(ClaimEpuPage)
      entryNumber <- userAnswers.get(ClaimEntryNumberPage)
      entryDate <- userAnswers.get(ClaimEntryDatePage)
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
      epu,
      entryNumber,
      entryDate,
      claimReason,
      claimDescription,
      LocalDate.now(),
      LocalDate.now(),
      payeeIndicator,
      paymentMethod)

    //TODO: Business decision to never send the VRN. API schema should be changed to replect this so we can change the UserDetails model
    def getAgentUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      eori <- userAnswers.get(EnterAgentEORIPage)
      name <- userAnswers.get(AgentNameImporterPage)
      address <- userAnswers.get(AgentImporterAddressPage)
    } yield UserDetails(
      None,
      eori,
      name,
      address
    )

    //TODO: Business decision to never send the VRN. API schema should be changed to reflect this so we can change the UserDetails model
    def getImporterUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      eori <- userAnswers.get(ImporterEoriPage)
      name <- userAnswers.get(ImporterNamePage)
      address <- userAnswers.get(ImporterAddressPage)
    } yield UserDetails(
      None,
      eori,
      name,
      address
    )

    def getBankDetails(userAnswers: UserAnswers): Option[AllBankDetails] = userAnswers.get(RepaymentTypePage) match {
      case Some(RepaymentType.BACS) =>
        for {
          bankDetails <- userAnswers.get(BankDetailsPage)
        } yield userAnswers.get(WhomToPayPage) match {
          case Some(Importer) => AllBankDetails(ImporterBankDetails = Some(bankDetails), AgentBankDetails = None)
          case Some(Representative) => AllBankDetails(ImporterBankDetails = None, AgentBankDetails = Some(bankDetails))
        }
      case _ => None
    }

    def cleanseBankDetails(bankDetails: BankDetails): BankDetails = bankDetails.copy(
        SortCode      = bankDetails.sortCodeTrimmed,
        AccountNumber = bankDetails.accountNumberPadded
    )

    def calcDutyClaimAmount(userAnswers: UserAnswers): Double = {
      userAnswers.get(CustomsDutyPaidPage).getOrElse("0.0").toDouble - userAnswers.get(CustomsDutyDueToHMRCPage).getOrElse("0.0").toDouble
    }

    def calcVatClaimAmount(userAnswers: UserAnswers): Double = {
      userAnswers.get(VATDueToHMRCPage).getOrElse("0.0").toDouble - userAnswers.get(VATPaidPage).getOrElse("0.0").toDouble
    }

    def calcOtherDutiesClaimAmount(userAnswers: UserAnswers): Double = {
      userAnswers.get(OtherDutiesDueToHMRCPage).getOrElse("0.0").toDouble - userAnswers.get(OtherDutiesPaidPage).getOrElse("0.0").toDouble
    }

    def getTypeTaxDetails(userAnswers: UserAnswers): Option[Seq[DutyTypeTaxDetails]] = for {
      customsDutyPaid <- userAnswers.get(CustomsDutyPaidPage).getOrElse("0.0")
      customsDutyDue <- userAnswers.get(CustomsDutyDueToHMRCPage).getOrElse("0.0")
      dutyClaimAmount <- calcDutyClaimAmount(userAnswers).toString
      vatDue <- userAnswers.get(VATDueToHMRCPage).getOrElse("0.0")
      vatPaid <- userAnswers.get(VATPaidPage).getOrElse("0.0")
      vatClaimAmount <- calcVatClaimAmount(userAnswers).toString
      otherDutiesDue <- userAnswers.get(OtherDutiesDueToHMRCPage).getOrElse("0.0")
      otherDutiesPaid <- userAnswers.get(OtherDutiesPaidPage).getOrElse("0.0")
      otherDutiesClaimAmount <- calcOtherDutiesClaimAmount(userAnswers).toString
    } yield DutyTypeTaxDetails(
      Seq(
        DutyTypeTaxList(ClaimRepaymentType.Customs, Some(customsDutyPaid.toString), Some(customsDutyDue.toString), Some(dutyClaimAmount.toString)),
        DutyTypeTaxList(ClaimRepaymentType.Vat, Some(vatPaid.toString),Some(vatDue.toString), Some(vatClaimAmount.toString)),
        DutyTypeTaxList(ClaimRepaymentType.Other, Some(otherDutiesPaid.toString),Some(otherDutiesDue.toString), Some(otherDutiesClaimAmount.toString))
      )
    )

    def getDocumentList(): Seq[DocumentList] = {
      Seq(DocumentList(EvidenceSupportingDocs.Other, None))
    }

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails: ClaimDetails <- getClaimDetails(userAnswers)
      agentDetails: UserDetails <- getAgentUserDetails(userAnswers)
      importerDetails: UserDetails <- getImporterUserDetails(userAnswers)
      bankDetails: AllBankDetails <- getBankDetails(userAnswers)
      dutyTypeTaxDetails: DutyTypeTaxDetails <- getTypeTaxDetails(userAnswers)
      documentList: DocumentList <- Seq(DocumentList(EvidenceSupportingDocs.Other, None))
    } yield Content(
      claimDetails,
      Some(agentDetails),
      importerDetails,
      Some(bankDetails),
      dutyTypeTaxDetails,
      Seq(documentList)
    )


    for {
      content <- getContent(userAnswers)
    } yield CreateClaimRequest(
      AcknowledgementReference("123456"),
      ApplicationType("NDRC"),
      OriginatingSystem("Digital"),
      content
    )
  }
}

