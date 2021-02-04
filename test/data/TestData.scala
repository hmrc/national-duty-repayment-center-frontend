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
import pages._

object TestData {

  val testInternalId: InternalId = InternalId("testInternalId")

  val testClaimantType: ClaimantType = ClaimantType.Importer
  val testEntryDetails: EntryDetails = EntryDetails("123", "123456A", LocalDate.parse("2012-12-12"))
  val testClaimDescription: ClaimDescription = ClaimDescription("this is a claim description")
  val testClaimRepaymentType: Seq[ClaimRepaymentType] = Seq(ClaimRepaymentType.Vat)
  val testCustomsDutyPaid: String = "100.00"
  val testCustomsDutyDueToHMRC: String = "50.00"
  val testImporterEORI: EORI = EORI("GB123456123456")
  val testImporterName: UserName = UserName("Joe", "Bloggs")
  val testImporterManualAddress: Address = Address("line 1", Some("line 2"), "City", Some("Region"), "GB", Some("AA11AA"))
  val testPhoneNumber: String = "01234567890"
  val testEmailAddress: String = "test@testing.com"
  val testBankDetails: BankDetails = BankDetails("account name", "123456", "12345678")

  def populateUserAnswersAgentWithEmail(userAnswers: UserAnswers): UserAnswers =
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
      .flatMap(_.set(ImporterHasEoriPage, true))
      .flatMap(_.set(IsVATRegisteredPage, IsVATRegistered.Yes))
      .flatMap(_.set(ImporterNamePage, testImporterName))
      .flatMap(_.set(ImporterManualAddressPage, testImporterManualAddress))
      .flatMap(_.set(PhoneNumberPage, testPhoneNumber))
      .flatMap(_.set(EmailAddressPage, testEmailAddress))
      .flatMap(_.set(RepaymentTypePage, RepaymentType.BACS))
      .flatMap(_.set(BankDetailsPage, testBankDetails))

      .get

}
