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

import models.AmendCaseResponseType.Furtherinformation
import models.FileUpload.Accepted
import models._
import models.requests.CreateClaimRequest
import pages._
import services.FileUploaded

import java.time.{LocalDate, ZoneId, ZonedDateTime}

object TestData {

  val testClaimantTypeRepresentative: ClaimantType = ClaimantType.Representative
  val testClaimantTypeImporter: ClaimantType = ClaimantType.Importer
  val testEntryDetails: EntryDetails = EntryDetails("123", "123456A", LocalDate.parse("2012-12-12"))
  val testClaimDescription: ClaimDescription = ClaimDescription("this is a claim description")
  val testClaimRepaymentType: Set[ClaimRepaymentType] = Set(ClaimRepaymentType.Customs)
  val amendCaseResponseType: Set[AmendCaseResponseType] = Set(AmendCaseResponseType.Furtherinformation, AmendCaseResponseType.Supportingdocuments)
  val referenceNumber: String = "P34567"
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
  val furtherInformation: String = "More info for amend"

  val testClaimDetails: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    Some(ArticleType.ErrorByCustoms),
    None,
    testClaimantTypeRepresentative,
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

  val testClaimDetailsWithRepresentativeAndMultipleEntries: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    Some(ArticleType.ErrorByCustoms),
    None,
    testClaimantTypeRepresentative,
    NumberOfEntriesType.Multiple,
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
    Some(ArticleType.ErrorByCustoms),
    None,
    testClaimantTypeRepresentative,
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

  val testClaimDetailsWithCMAAndImporter: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    Some(ArticleType.ErrorByCustoms),
    None,
    testClaimantTypeImporter,
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

  val testClaimDetailsWithUKCustomsRegulationType: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UKCustomsCodeRegulation,
    None,
    Some(UkRegulationType.Rejected),
    testClaimantTypeImporter,
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

  val testImporterDetails: UserDetails = UserDetails(
    "true",
    testImporterEORI,
    testImporterName,
    testImporterManualAddress,
    Some(testPhoneNumber),
    Some(testEmailAddress)
  )

  val testDutyTypeTaxDetails: DutyTypeTaxDetails = DutyTypeTaxDetails(
    Seq(
      DutyTypeTaxList(ClaimRepaymentType.Customs, "100.00", "50.00", "50.0"),
      DutyTypeTaxList(ClaimRepaymentType.Vat, "0.0", "0.0", "0.0"),
      DutyTypeTaxList(ClaimRepaymentType.Other, "0.0", "0.0", "0.0"),

    )
  )

  def populateUserAnswersWithRepresentativeAndMultipleEntries(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, NumberOfEntriesType.Multiple))
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
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(AgentNameImporterPage, testImporterName))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(ImporterNamePage, testAgentName))
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(WhomToPayPage, testWhomToPay))
      .flatMap(_.set(BankDetailsPage, testBankDetails))
      .get

  def populateUserAnswersRepresentativeWithEmail(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
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
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
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
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
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
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
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

  def populateUserAnswersWithCMAPaymentMethodAndClaimantImporter(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeImporter)
      .flatMap(_.set(NumberOfEntriesTypePage, NumberOfEntriesType.Single))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testCustomsDutyPaid))
      .flatMap(_.set(CustomsDutyDueToHMRCPage, testCustomsDutyDueToHMRC))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testImporterEORI))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .get

  def populateUserAnswersWithUKCustomsRegulationType(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeImporter)
      .flatMap(_.set(NumberOfEntriesTypePage, NumberOfEntriesType.Single))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation))
      .flatMap(_.set(UkRegulationTypePage, UkRegulationType.Rejected))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testCustomsDutyPaid))
      .flatMap(_.set(CustomsDutyDueToHMRCPage, testCustomsDutyDueToHMRC))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testImporterEORI))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .get

  def populateUserAnswersWithAmendData(userAnswers: UserAnswers): UserAnswers =
    userAnswers.copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(AmendCaseResponseTypePage, amendCaseResponseType)
      .flatMap(_.set(ReferenceNumberPage, referenceNumber))
      .flatMap(_.set(FurtherInformationPage, furtherInformation))
      .get

  val fileUploaded =
    Accepted(
      orderNumber = 1,
      reference = "ref-123",
      url = "/bucket/test1.jpeg",
      uploadTimestamp = ZonedDateTime.of(2020, 10, 10, 10, 10, 10, 0, ZoneId.of("UTC")),
      checksum = "f55a741917d512ab4c547ea97bdfdd8df72bed5fe51b6a248e0a5a0ae58061c8",
      fileName = "test1.jpeg",
      fileMimeType = "image/jpeg"
    )

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

  val testCreateClaimRequestWithRepresentativeAndMultipleEntries: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithRepresentativeAndMultipleEntries,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      Some(AllBankDetails(Some(testBankDetails), None)),
      testDutyTypeTaxDetails,
      testDocumentList), Nil
  )

  testCreateClaimRequestWithRepresentativeAndMultipleEntries

  val testCreateClaimRequestWithCMAPaymentMethodAndClaimantImporter: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithCMAAndImporter,
      None,
      testImporterDetails,
      None,
      testDutyTypeTaxDetails,
      testDocumentList), Nil
  )

  val testCreateClaimRequestWithUKCustomsRegulationType: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithUKCustomsRegulationType,
      None,
      testImporterDetails,
      None,
      testDutyTypeTaxDetails,
      testDocumentList), Nil
  )

}
