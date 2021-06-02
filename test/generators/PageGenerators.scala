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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryAmendCaseUploadAnotherFilePage: Arbitrary[AmendCaseUploadAnotherFilePage.type] =
    Arbitrary(AmendCaseUploadAnotherFilePage)

  implicit lazy val arbitraryAmendCaseSendInformationPage: Arbitrary[AmendCaseSendInformationPage.type] =
    Arbitrary(AmendCaseSendInformationPage)

  implicit lazy val arbitraryAmendCaseResponseTypePage: Arbitrary[AmendCaseResponseTypePage.type] =
    Arbitrary(AmendCaseResponseTypePage)

  implicit lazy val arbitraryFurtherInformationPage: Arbitrary[FurtherInformationPage.type] =
    Arbitrary(FurtherInformationPage)

  implicit lazy val arbitraryReferenceNumberPage: Arbitrary[ReferenceNumberPage.type] =
    Arbitrary(ReferenceNumberPage)

  implicit lazy val arbitraryBulkFileUploadPage: Arbitrary[BulkFileUploadPage.type] =
    Arbitrary(BulkFileUploadPage)

  implicit lazy val arbitraryIndirectRepresentativePage: Arbitrary[IndirectRepresentativePage.type] =
    Arbitrary(IndirectRepresentativePage)

  implicit lazy val arbitraryBankDetailsPage: Arbitrary[BankDetailsPage.type] =
    Arbitrary(BankDetailsPage)

  implicit lazy val arbitraryAgentImporterManualAddressPage: Arbitrary[AgentImporterManualAddressPage.type] =
    Arbitrary(AgentImporterManualAddressPage)

  implicit lazy val arbitraryImporterManualAddressPage: Arbitrary[ImporterManualAddressPage.type] =
    Arbitrary(ImporterManualAddressPage)

  implicit lazy val arbitraryAgentImporterAddressPage: Arbitrary[AgentImporterAddressPage.type] =
    Arbitrary(AgentImporterAddressPage)

  implicit lazy val arbitraryImporterAddressPage: Arbitrary[ImporterAddressPage.type] =
    Arbitrary(ImporterAddressPage)

  implicit lazy val arbitraryOtherDutiesPaidPage: Arbitrary[OtherDutiesPaidPage.type] =
    Arbitrary(OtherDutiesPaidPage)

  implicit lazy val arbitraryCustomsDutyPaidPage: Arbitrary[CustomsDutyPaidPage.type] =
    Arbitrary(CustomsDutyPaidPage)

  implicit lazy val arbitraryVATPaidPage: Arbitrary[VATPaidPage.type] =
    Arbitrary(VATPaidPage)

  implicit lazy val arbitraryAgentImporterHasEORIPage: Arbitrary[AgentImporterHasEORIPage.type] =
    Arbitrary(AgentImporterHasEORIPage)

  implicit lazy val arbitraryIsImporterVatRegisteredPage: Arbitrary[IsImporterVatRegisteredPage.type] =
    Arbitrary(IsImporterVatRegisteredPage)

  implicit lazy val arbitraryEnterAgentEORIPage: Arbitrary[EnterAgentEORIPage.type] =
    Arbitrary(EnterAgentEORIPage)

  implicit lazy val arbitraryWhomToPayPage: Arbitrary[WhomToPayPage.type] =
    Arbitrary(WhomToPayPage)

  implicit lazy val arbitraryRepaymentTypePage: Arbitrary[RepaymentTypePage.type] =
    Arbitrary(RepaymentTypePage)

  implicit lazy val arbitraryRepresentativeAgentNameImporterPage
    : Arbitrary[RepresentativeDeclarantAndBusinessNamePage.type] =
    Arbitrary(RepresentativeDeclarantAndBusinessNamePage)

  implicit lazy val arbitraryRepresentativeImporterNameImporterPage: Arbitrary[RepresentativeImporterNamePage.type] =
    Arbitrary(RepresentativeImporterNamePage)

  implicit lazy val arbitraryEmailAddressAndPhoneNumberPage: Arbitrary[EmailAddressAndPhoneNumberPage.type] =
    Arbitrary(EmailAddressAndPhoneNumberPage)

  implicit lazy val arbitraryImporterNamePage: Arbitrary[ImporterNamePage.type] =
    Arbitrary(ImporterNamePage)

  implicit lazy val arbitraryDeclarantNamePage: Arbitrary[DeclarantNamePage.type] =
    Arbitrary(DeclarantNamePage)

  implicit lazy val arbitraryEvidenceSupportingDocsPage: Arbitrary[EvidenceSupportingDocsPage.type] =
    Arbitrary(EvidenceSupportingDocsPage)

  implicit lazy val arbitraryClaimRepaymentTypePage: Arbitrary[ClaimRepaymentTypePage.type] =
    Arbitrary(ClaimRepaymentTypePage)

  implicit lazy val arbitraryReasonForOverpaymentPage: Arbitrary[ReasonForOverpaymentPage.type] =
    Arbitrary(ReasonForOverpaymentPage)

  implicit lazy val arbitraryClaimReasonTypePage: Arbitrary[ClaimReasonTypePage.type] =
    Arbitrary(ClaimReasonTypePage)

  implicit lazy val arbitraryEntryDetailsPage: Arbitrary[EntryDetailsPage.type] =
    Arbitrary(EntryDetailsPage)

  implicit lazy val arbitraryNumberOfEntriesTypePage: Arbitrary[NumberOfEntriesTypePage.type] =
    Arbitrary(NumberOfEntriesTypePage)

  implicit lazy val arbitraryArticleTypePage: Arbitrary[ArticleTypePage.type] =
    Arbitrary(ArticleTypePage)

  implicit lazy val arbitraryCustomsRegulationTypePage: Arbitrary[CustomsRegulationTypePage.type] =
    Arbitrary(CustomsRegulationTypePage)

  implicit lazy val arbitraryIsVATRegisteredPage: Arbitrary[IsVATRegisteredPage.type] =
    Arbitrary(IsVATRegisteredPage)

  implicit lazy val arbitraryImporterEoriPage: Arbitrary[ImporterEoriPage.type] =
    Arbitrary(ImporterEoriPage)

  implicit lazy val arbitraryImporterHasEoriPage: Arbitrary[ImporterHasEoriPage.type] =
    Arbitrary(ImporterHasEoriPage)

  implicit lazy val arbitraryClaimantTypePage: Arbitrary[ClaimantTypePage.type] =
    Arbitrary(ClaimantTypePage)

  implicit lazy val arbitraryDoYouOwnTheGoodsPage: Arbitrary[DoYouOwnTheGoodsPage.type] =
    Arbitrary(DoYouOwnTheGoodsPage)

}
