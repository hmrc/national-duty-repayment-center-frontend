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

import forms.EmailAndPhoneNumber
import models.FileUpload.Accepted
import models._
import models.requests.CreateClaimRequest
import pages._
import services.FileUploaded

import java.time.{LocalDate, ZoneId, ZonedDateTime}

object TestData {

  val testClaimantTypeRepresentative: ClaimantType    = ClaimantType.Representative
  val testClaimantTypeImporter: ClaimantType          = ClaimantType.Importer
  val testEntryDetails: EntryDetails                  = EntryDetails("123", "123456A", LocalDate.parse("2012-12-12"))
  val testClaimDescription: ClaimDescription          = ClaimDescription("this is a claim description")
  val testClaimRepaymentType: Set[ClaimRepaymentType] = Set(ClaimRepaymentType.Customs)

  val amendCaseResponseType: Set[AmendCaseResponseType] =
    Set(AmendCaseResponseType.FurtherInformation, AmendCaseResponseType.SupportingDocuments)

  val referenceNumber: String                        = "P34567"
  val testRepaymentAmounts: RepaymentAmounts         = RepaymentAmounts("100.00", "50.00")
  val testAgentImporterHasEORI: AgentImporterHasEORI = AgentImporterHasEORI.Yes
  val testImporterEORI: EORI                         = EORI("GB123456123456")
  val testAgentEORI: EORI                            = EORI("GB123456123444")
  val testRepresentativeAgentName: String            = "Agent name"
  val testRepresentativeImporterName: UserName       = UserName("ImporterName")
  val testImporterManualAddress: Address             = Address("line 1", Some("line 2"), "City", Some("Region"), "GB", "AA11AA")

  val testAgentManualAddress: Address =
    Address("line 1 agent", Some("line 2 agent"), "City agent", Some("Region agent"), "IT", "AA11AA")

  val testEmailAndPhoneNumber: EmailAndPhoneNumber = EmailAndPhoneNumber(
    Set(IsContactProvided.Email, IsContactProvided.Phone),
    Some("test@testing.com"),
    Some("01234567890")
  )

  val testWhomToPay: WhomToPay                = WhomToPay.Importer
  val testWhomToPayRepresentative: WhomToPay  = WhomToPay.Representative
  val testWhomToPayCMA: WhomToPay             = WhomToPay.CMA
  val testBankDetails: BankDetails            = BankDetails("account name", "123456", "12345678")
  val testBankDetailsWith6Digits: BankDetails = BankDetails("account name", "123456", "123456")
  val testPaddedBankDetails: BankDetails      = BankDetails("account name", "123456", "00123456")
  val testDocumentList: Seq[DocumentList]     = Seq(DocumentList(EvidenceSupportingDocs.Other, None))
  val furtherInformation: String              = "More info for amend"
  val testDeclarantRefNumber: String          = "12345"
  val testDeclarantName: Name                 = Name("Declarant", "One")
  val testRepresentativeDeclarantName: String = "Representative declarant name"
  val testImporterName: UserName              = UserName("Importer One")

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
    testDeclarantRefNumber,
    testRepresentativeDeclarantName
  )

  val testClaimDetailsWithRepresentativeAndMultipleEntries: ClaimDetails = ClaimDetails(
    FormType("01"),
    CustomsRegulationType.UnionsCustomsCodeRegulation,
    Some(ArticleType.ErrorByCustoms),
    None,
    testClaimantTypeRepresentative,
    NumberOfEntriesType.Multiple,
    Some("2"),
    testEntryDetails,
    ClaimReasonType.Cpuchange,
    testClaimDescription,
    LocalDate.now(),
    LocalDate.now(),
    testWhomToPay,
    RepaymentType.BACS,
    testDeclarantRefNumber,
    testRepresentativeDeclarantName
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
    testDeclarantRefNumber,
    testRepresentativeDeclarantName
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
    testDeclarantRefNumber,
    testDeclarantName.toString
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
    testDeclarantRefNumber,
    testDeclarantName.toString
  )

  val testClaimDetailsWithRepresentativeSinglePayingRepresentativeBacs: ClaimDetails = ClaimDetails(
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
    testWhomToPayRepresentative,
    RepaymentType.BACS,
    testDeclarantRefNumber,
    testRepresentativeDeclarantName
  )

  val testAgentDetails: UserDetails = UserDetails(
    "false",
    testAgentEORI,
    testRepresentativeAgentName.toString,
    testAgentManualAddress,
    testEmailAndPhoneNumber.phone,
    testEmailAndPhoneNumber.email
  )

  val testImporterDetailsRepresentativeJourney: UserDetails =
    UserDetails("true", testImporterEORI, testRepresentativeImporterName.value, testImporterManualAddress, None, None)

  val testImporterDetails: UserDetails = UserDetails(
    "true",
    testImporterEORI,
    testImporterName.value,
    testImporterManualAddress,
    testEmailAndPhoneNumber.phone,
    testEmailAndPhoneNumber.email
  )

  val testDutyTypeTaxDetails: DutyTypeTaxDetails = DutyTypeTaxDetails(
    Seq(
      DutyTypeTaxList(ClaimRepaymentType.Customs, "100.00", "50.00", "50.00"),
      DutyTypeTaxList(ClaimRepaymentType.Vat, "0.00", "0.00", "0.00"),
      DutyTypeTaxList(ClaimRepaymentType.Other, "0.00", "0.00", "0.00")
    )
  )

  def populateUserAnswersWithRepresentativeAndMultipleEntries(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(RepresentativeImporterNamePage, testRepresentativeImporterName))
      .flatMap(
        _.set(
          RepresentativeDeclarantAndBusinessNamePage,
          RepresentativeDeclarantAndBusinessName(testRepresentativeDeclarantName, testRepresentativeAgentName)
        )
      )
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(WhomToPayPage, testWhomToPay))
      .flatMap(_.set(BankDetailsPage, testBankDetails))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersRepresentativeWithEmail(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(RepresentativeImporterNamePage, testRepresentativeImporterName))
      .flatMap(
        _.set(
          RepresentativeDeclarantAndBusinessNamePage,
          RepresentativeDeclarantAndBusinessName(testRepresentativeDeclarantName, testRepresentativeAgentName)
        )
      )
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.BACS))
      .flatMap(_.set(WhomToPayPage, testWhomToPay))
      .flatMap(_.set(BankDetailsPage, testBankDetails))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithCMAPaymentMethod(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(RepresentativeImporterNamePage, testRepresentativeImporterName))
      .flatMap(
        _.set(
          RepresentativeDeclarantAndBusinessNamePage,
          RepresentativeDeclarantAndBusinessName(testRepresentativeDeclarantName, testRepresentativeAgentName)
        )
      )
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithCMAPaymentMethodAndClaimantImporter(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeImporter)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testImporterEORI))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.No))
      .flatMap(_.set(DeclarantNamePage, testDeclarantName))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithUKCustomsRegulationType(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeImporter)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UKCustomsCodeRegulation))
      .flatMap(_.set(UkRegulationTypePage, UkRegulationType.Rejected))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testImporterEORI))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(DoYouOwnTheGoodsPage, DoYouOwnTheGoods.No))
      .flatMap(_.set(DeclarantNamePage, testDeclarantName))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.CMA))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithRepresentativeSinglePayingRepresentativeBacs(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(RepresentativeImporterNamePage, testRepresentativeImporterName))
      .flatMap(
        _.set(
          RepresentativeDeclarantAndBusinessNamePage,
          RepresentativeDeclarantAndBusinessName(testRepresentativeDeclarantName, testRepresentativeAgentName)
        )
      )
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.BACS))
      .flatMap(_.set(WhomToPayPage, testWhomToPayRepresentative))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithBankAccountNumberContaining6Digits(userAnswers: UserAnswers): UserAnswers =
    userAnswers
      .set(ClaimantTypePage, testClaimantTypeRepresentative)
      .flatMap(_.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Single, None)))
      .flatMap(_.set(CustomsRegulationTypePage, CustomsRegulationType.UnionsCustomsCodeRegulation))
      .flatMap(_.set(ArticleTypePage, ArticleType.ErrorByCustoms))
      .flatMap(_.set(EntryDetailsPage, testEntryDetails))
      .flatMap(_.set(ClaimReasonTypePage, ClaimReasonType.Cpuchange))
      .flatMap(_.set(ReasonForOverpaymentPage, testClaimDescription))
      .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
      .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
      .flatMap(_.set(AgentImporterHasEORIPage, testAgentImporterHasEORI))
      .flatMap(_.set(EnterAgentEORIPage, testImporterEORI))
      .flatMap(_.set(IsImporterVatRegisteredPage, IsImporterVatRegistered.Yes))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(ImporterEoriPage, testAgentEORI))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(RepresentativeImporterNamePage, testRepresentativeImporterName))
      .flatMap(
        _.set(
          RepresentativeDeclarantAndBusinessNamePage,
          RepresentativeDeclarantAndBusinessName(testRepresentativeDeclarantName, testRepresentativeAgentName)
        )
      )
      .flatMap(_.set(AgentImporterManualAddressPage, testAgentManualAddress))
      .flatMap(_.set(EmailAddressAndPhoneNumberPage, testEmailAndPhoneNumber))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.BACS))
      .flatMap(_.set(BankDetailsPage, testBankDetailsWith6Digits))
      .flatMap(_.set(WhomToPayPage, testWhomToPayRepresentative))
      .flatMap(
        _.set(
          DeclarantReferenceNumberPage,
          DeclarantReferenceNumber(DeclarantReferenceType.Yes, Some(testDeclarantRefNumber))
        )
      )
      .get

  def populateUserAnswersWithAmendData(userAnswers: UserAnswers): UserAnswers =
    userAnswers.copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(AmendCaseResponseTypePage, amendCaseResponseType)
      .flatMap(_.set(ReferenceNumberPage, referenceNumber))
      .flatMap(_.set(FurtherInformationPage, furtherInformation))
      .get

  def populateUserAnswersWithImporterInformation(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersWithCMAPaymentMethodAndClaimantImporter(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(BankDetailsPage, testBankDetails)
      .get

  def populateUserAnswersWithImporterUKCustomsRegulationInformation(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersWithUKCustomsRegulationType(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(BankDetailsPage, testBankDetails)
      .get

  def populateUserAnswersWithRepresentativeSingleBACSJourney(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersRepresentativeWithEmail(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))

  def populateUserAnswersWithRepresentativeSingleCMAJourney(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersWithCMAPaymentMethod(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(BankDetailsPage, testBankDetails)
      .get

  def populateUserAnswersWithRepresentativeMultipleJourney(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersWithRepresentativeAndMultipleEntries(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded, bulkFileUploaded)))))
      .set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("3")))
      .get

  def populateUserAnswersWithRepresentativeSinglePayingRepresentativeJourney(userAnswers: UserAnswers): UserAnswers =
    populateUserAnswersWithRepresentativeSinglePayingRepresentativeBacs(userAnswers)
      .copy(fileUploadState = Some(FileUploaded(fileUploads = FileUploads(Seq(fileUploaded)))))
      .set(IndirectRepresentativePage, true)
      .flatMap(_.set(BankDetailsPage, testBankDetails))
      .get

  val fileUploaded =
    Accepted(
      orderNumber = 1,
      reference = "ref-123",
      url = "/bucket/test1.jpeg",
      uploadTimestamp = ZonedDateTime.of(2020, 10, 10, 10, 10, 10, 0, ZoneId.of("UTC")),
      checksum = "f55a741917d512ab4c547ea97bdfdd8df72bed5fe51b6a248e0a5a0ae58061c8",
      fileName = "test1.jpeg",
      fileMimeType = "image/jpeg",
      fileType = Some(FileType.SupportingEvidence)
    )

  val bulkFileUploaded =
    Accepted(
      orderNumber = 2,
      reference = "ref-124",
      url = "/bucket/test2.jpeg",
      uploadTimestamp = ZonedDateTime.of(2020, 10, 10, 10, 10, 10, 0, ZoneId.of("UTC")),
      checksum = "f55a741917d512ab4c547ea97bdfdd8df72bed5fe51b6a248e0a5a0ae58061c8",
      fileName = "test2.jpeg",
      fileMimeType = "image/jpeg",
      fileType = Some(FileType.Bulk)
    )

  val testCreateClaimRequestRepresentativeWithEmail: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetails,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      Some(AllBankDetails(Some(testBankDetails), None)),
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithCMAPaymentMethod: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithCMA,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      None,
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithRepresentativeAndMultipleEntries: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithRepresentativeAndMultipleEntries,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      Some(AllBankDetails(Some(testBankDetails), None)),
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithCMAPaymentMethodAndClaimantImporter: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithCMAAndImporter,
      None,
      testImporterDetails,
      None,
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithUKCustomsRegulationType: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithUKCustomsRegulationType,
      None,
      testImporterDetails,
      None,
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithRepresentativeSinglePayingRepresentativeBacs: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithRepresentativeSinglePayingRepresentativeBacs,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      None,
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

  val testCreateClaimRequestWithBankAccountNumberContaining6Digits: CreateClaimRequest = CreateClaimRequest(
    Content(
      testClaimDetailsWithRepresentativeSinglePayingRepresentativeBacs,
      Some(testAgentDetails),
      testImporterDetailsRepresentativeJourney,
      Some(AllBankDetails(None, Some(testPaddedBankDetails))),
      testDutyTypeTaxDetails,
      testDocumentList
    ),
    Nil
  )

}
