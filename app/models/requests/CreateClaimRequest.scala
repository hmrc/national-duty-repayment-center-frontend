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
      paymentMethod)

    //TODO: Business decision to never send the VRN. API schema should be changed to replect this so we can change the UserDetails model
    def getAgentUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      eori <- userAnswers.get(EnterAgentEORIPage)
      name <- userAnswers.get(AgentNameImporterPage)
      address <- userAnswers.get(AgentImporterAddressPage)
      telephone <- userAnswers.get(PhoneNumberPage)
      email <-userAnswers.get(EmailAddressPage)
    } yield UserDetails(
      None,
      eori,
      name,
      address,
      Some(telephone),
      Some(email)
    )

    //TODO: Business decision to never send the VRN. API schema should be changed to reflect this so we can change the UserDetails model
    def getImporterUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      eori <- userAnswers.get(ImporterEoriPage)
      name <- userAnswers.get(ImporterNamePage)
      address <- userAnswers.get(ImporterAddressPage)
      telephone <- userAnswers.get(PhoneNumberPage)
      email <-userAnswers.get(EmailAddressPage)
    } yield UserDetails(
      None,
      eori,
      name,
      address,
      Some(telephone),
      Some(email)
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

    def getTypeTaxDetails(userAnswers: UserAnswers): Option[DutyTypeTaxDetails] = for {
      customsDutyPaid <- userAnswers.get(CustomsDutyPaidPage)
      customsDutyDue <- userAnswers.get(CustomsDutyDueToHMRCPage)
      vatPaid <- userAnswers.get(VATPaidPage)
      vatDue <- userAnswers.get(VATDueToHMRCPage)
      otherDutiesPaid <- userAnswers.get(OtherDutiesPaidPage)
      otherDutiesDue <- userAnswers.get(OtherDutiesDueToHMRCPage)
    } yield {
      val dutyClaimAmount: String = (customsDutyPaid.toDouble - customsDutyDue.toDouble).toString
      val vatClaimAmount: String = (vatPaid.toDouble - vatDue.toDouble).toString
      val otherDutiesClaimAmount: String = (otherDutiesPaid.toDouble - otherDutiesDue.toDouble).toString
      DutyTypeTaxDetails(
        Seq(
          DutyTypeTaxList(ClaimRepaymentType.Customs, Some(customsDutyPaid), Some(customsDutyDue), Some(dutyClaimAmount)),
          DutyTypeTaxList(ClaimRepaymentType.Vat, Some(vatPaid),Some(vatDue), Some(vatClaimAmount)),
          DutyTypeTaxList(ClaimRepaymentType.Other, Some(otherDutiesPaid),Some(otherDutiesDue), Some(otherDutiesClaimAmount))
        )
      )
    }

    def getDocumentList(): DocumentList = {
      DocumentList(EvidenceSupportingDocs.Other, None)
    }

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails <- getClaimDetails(userAnswers)
      agentDetails <- getAgentUserDetails(userAnswers)
      importerDetails <- getImporterUserDetails(userAnswers)
      bankDetails <- getBankDetails(userAnswers)
      dutyTypeTaxDetails <- getTypeTaxDetails(userAnswers)
    } yield {
      val documentList = getDocumentList()
      Content(
        claimDetails,
        Some(agentDetails),
        importerDetails,
        Some(bankDetails),
        dutyTypeTaxDetails,
        Seq(documentList)
      )
    }


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

