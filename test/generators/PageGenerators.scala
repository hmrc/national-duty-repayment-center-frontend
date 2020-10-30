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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryCustomsDutyDueToHMRCPage: Arbitrary[CustomsDutyDueToHMRCPage.type] =
    Arbitrary(CustomsDutyDueToHMRCPage)

  implicit lazy val arbitrarycustomsDutyPaidPage: Arbitrary[customsDutyPaidPage.type] =
    Arbitrary(customsDutyPaidPage)

  implicit lazy val arbitraryVATDueToHMRCPage: Arbitrary[VATDueToHMRCPage.type] =
    Arbitrary(VATDueToHMRCPage)

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



  implicit lazy val arbitraryAgentNameImporterPage: Arbitrary[AgentNameImporterPage.type] =
    Arbitrary(AgentNameImporterPage)

  implicit lazy val arbitraryPhoneNumberPage: Arbitrary[PhoneNumberPage.type] =
    Arbitrary(PhoneNumberPage)

  implicit lazy val arbitraryEmailAddressPage: Arbitrary[EmailAddressPage.type] =
    Arbitrary(EmailAddressPage)


  implicit lazy val arbitraryContactTypePage: Arbitrary[ContactTypePage.type] =
    Arbitrary(ContactTypePage)

  implicit lazy val arbitraryImporterNamePage: Arbitrary[ImporterNamePage.type] =
    Arbitrary(ImporterNamePage)

  implicit lazy val arbitraryEvidenceSupportingDocsPage: Arbitrary[EvidenceSupportingDocsPage.type] =
    Arbitrary(EvidenceSupportingDocsPage)

  implicit lazy val arbitraryClaimRepaymentTypePage: Arbitrary[ClaimRepaymentTypePage.type] =
    Arbitrary(ClaimRepaymentTypePage)

  implicit lazy val arbitraryReasonForOverpaymentPage: Arbitrary[ReasonForOverpaymentPage.type] =
    Arbitrary(ReasonForOverpaymentPage)

  implicit lazy val arbitraryWhatAreTheGoodsPage: Arbitrary[WhatAreTheGoodsPage.type] =
    Arbitrary(WhatAreTheGoodsPage)

  implicit lazy val arbitraryClaimReasonTypePage: Arbitrary[ClaimReasonTypePage.type] =
    Arbitrary(ClaimReasonTypePage)

  implicit lazy val arbitraryClaimEntryDatePage: Arbitrary[ClaimEntryDatePage.type] =
    Arbitrary(ClaimEntryDatePage)

  implicit lazy val arbitraryClaimEntryNumberPage: Arbitrary[ClaimEntryNumberPage.type] =
    Arbitrary(ClaimEntryNumberPage)

  implicit lazy val arbitraryClaimEpuPage: Arbitrary[ClaimEpuPage.type] =
    Arbitrary(ClaimEpuPage)

  implicit lazy val arbitraryHowManyEntriesPage: Arbitrary[HowManyEntriesPage.type] =
    Arbitrary(HowManyEntriesPage)

  implicit lazy val arbitraryNumberOfEntriesTypePage: Arbitrary[NumberOfEntriesTypePage.type] =
    Arbitrary(NumberOfEntriesTypePage)

  implicit lazy val arbitraryArticleTypePage: Arbitrary[ArticleTypePage.type] =
    Arbitrary(ArticleTypePage)

  implicit lazy val arbitraryCustomsRegulationTypePage: Arbitrary[CustomsRegulationTypePage.type] =
    Arbitrary(CustomsRegulationTypePage)

  implicit lazy val arbitraryImporterClaimantVrnPage: Arbitrary[ImporterClaimantVrnPage.type] =
    Arbitrary(ImporterClaimantVrnPage)

  implicit lazy val arbitraryIsVatRegisteredPage: Arbitrary[IsVatRegisteredPage.type] =
    Arbitrary(IsVatRegisteredPage)

  implicit lazy val arbitraryImporterEoriPage: Arbitrary[ImporterEoriPage.type] =
    Arbitrary(ImporterEoriPage)

  implicit lazy val arbitraryImporterHasEoriPage: Arbitrary[ImporterHasEoriPage.type] =
    Arbitrary(ImporterHasEoriPage)

  implicit lazy val arbitraryClaimantTypePage: Arbitrary[ClaimantTypePage.type] =
    Arbitrary(ClaimantTypePage)
}
