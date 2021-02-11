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

package data

import java.time.LocalDate

import models._
import models.requests.CreateClaimRequest
import pages._

object TestData {

  val testClaimantType: ClaimantType = ClaimantType.Representative
  val testEntryDetails: EntryDetails = EntryDetails("123", "123456A", LocalDate.parse("2012-12-12"))
  val testClaimDescription: ClaimDescription = ClaimDescription("this is a claim description")
  val testClaimRepaymentType: Set[ClaimRepaymentType] = Set(ClaimRepaymentType.Customs)
  val testCustomsDutyPaid: String = "100.00"
  val testCustomsDutyDueToHMRC: String = "50.00"
  val testAgentImporterHasEORI: AgentImporterHasEORI = AgentImporterHasEORI.Yes
  val testImporterEORI: EORI = EORI("GB123456123456")
  val testAgentEORI: EORI = EORI("GB123456123444")
  val testAgentName: UserName = UserName("agent first", "agent last")
  val testImporterName: UserName = UserName("importer first", "importer last")
  val testImporterManualAddress: Address = Address("line 1", Some("line 2"), "City", Some("Region"), "GB", Some("AA11AA"))
  val testAgentManualAddress: Address = Address("line 1 agent", Some("line 2 agent"), "City agent", Some("Region agent"), "IT", None)
  val testPhoneNumber: String = "01234567890"
  val testEmailAddress: String = "test@testing.com"
  val testWhomToPay: WhomToPay = WhomToPay.Importer
  val testWhomToPayCMA: WhomToPay = WhomToPay.CMA
  val testBankDetails: BankDetails = BankDetails("account name", "123456", "12345678")
  val testDocumentList: Seq[DocumentList] = Seq(DocumentList(EvidenceSupportingDocs.Other, None))

  val testClaimDetails: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    ArticleType.ErrorByCustoms,
    testClaimantType,
    NumberOfEntriesType.Single,
    None,
    testEntryDetails,
    ClaimReasonType.Cpuchange,
    testClaimDescription,
    LocalDate.now(),
    LocalDate.now(),
    testWhomToPay,
    RepaymentType.BACS,
    "NA"
  )

  val testClaimDetailsWithCMA: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    ArticleType.ErrorByCustoms,
    testClaimantType,
    NumberOfEntriesType.Single,
    None,
    testEntryDetails,
    ClaimReasonType.Cpuchange,
    testClaimDescription,
    LocalDate.now(),
    LocalDate.now(),
    testWhomToPayCMA,
    RepaymentType.CMA,
    "NA"
  )

  val testAgentDetails: UserDetails = UserDetails(
    "false",
    testAgentEORI,
    testAgentName,
    testAgentManualAddress,
    Some(testPhoneNumber),
    Some(testEmailAddress)
  )

  val testImporterDetailsRepresentativeJourney: UserDetails = UserDetails(
    "true",
    testImporterEORI,
    testImporterName,
    testImporterManualAddress,
    None,
    None
  )

  val testDutyTypeTaxDetails: DutyTypeTaxDetails = DutyTypeTaxDetails(
    Seq(
      DutyTypeTaxList(ClaimRepaymentType.Customs, "100.00", "50.00", "50.0"),
      DutyTypeTaxList(ClaimRepaymentType.Vat, "0.0", "0.0", "0.0"),
      DutyTypeTaxList(ClaimRepaymentType.Other, "0.0", "0.0", "0.0"),

    )
  )

  def populateUserAnswersRepresentativeWithEmail(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantType)
      .flatMap(_.set(NumberOfEntriesTypePage, NumberOfEntriesType.Single))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testCustomsDutyPaid))
      .flatMap(_.set(CustomsDutyDueToHMRCPage, testCustomsDutyDueToHMRC))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, true))
      .flatMap(_.set(AgentNameImporterPage, testImporterName))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(ImporterNamePage, testAgentName))
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.BACS))
      .flatMap(_.set(WhomToPayPage, testWhomToPay))
      .flatMap(_.set(BankDetailsPage, testBankDetails))
      .get

  def populateUserAnswersWithCMAPaymentMethod(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantType)
      .flatMap(_.set(NumberOfEntriesTypePage, NumberOfEntriesType.Single))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testCustomsDutyPaid))
      .flatMap(_.set(CustomsDutyDueToHMRCPage, testCustomsDutyDueToHMRC))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, true))
      .flatMap(_.set(AgentNameImporterPage, testImporterName))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(ImporterNamePage, testAgentName))
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .get

  val testCreateClaimRequestRepresentativeWithEmail: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetails,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      Some(AllBankDetails(Some(testBankDetails), None)),
      testDutyTypeTaxDetails,
      testDocumentList), Nil
  )

  val testCreateClaimRequestWithCMAPaymentMethod: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithCMA,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      None,
      testDutyTypeTaxDetails,
      testDocumentList), Nil
  )

}
